import typing as t
import pydantic as p
from datetime import timedelta

from flask import Flask, request, make_response
from flask_jwt_extended import JWTManager, jwt_required, create_access_token, get_jwt
from flask_mysqldb import MySQL
from PIL import Image
import numpy as np

from lpr import plate_number_recognition
import json_response
import models as m
import db

app = Flask('smart_parking')
app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'root'
app.config['MYSQL_PASSWORD'] = 'root'
app.config['MYSQL_DATABASE'] = db.DB_NAME
app.config['JWT_SECRET_KEY'] = '5ccf5872f8ccf34f10e7b43a6ddfd404987c9b095b289b3db658b9c1c1b913c5'
jwt = JWTManager(app)
mysql = MySQL(app)

RequestSchema = t.TypeVar("RequestSchema")


@app.before_request
def set_db():
    with mysql.connection.cursor() as cursor:
        cursor.execute(f'USE {db.DB_NAME}')


def get_request_data(request_schema: t.Type[RequestSchema]) -> RequestSchema:
    """Parse json body from request and validate with the given schema. If validation fails,
    immediately return a 400 error response to the client.
    """
    try:
        return request_schema(**request.get_json())
    except p.ValidationError as exc:
        json_response.abort(exc.errors(), 400)
    except TypeError:
        json_response.abort([{'msg': 'Wrong content type. Must be json.'}], 400)


@app.route('/login', methods=['POST'])
def login():
    req = get_request_data(m.LoginRequest)
    user = db.login_user(req, mysql.connection)

    if user is not None:
        additional_claims = {
            'user_id': user.id,
            'username': user.username,
            'first_name': user.firstName,
            'last_name': user.lastName,
            'phone': user.phone,
            'organization_id': user.organizationId
        }

        resp = m.LoginResponse(
            user=user,
            token=create_access_token(
                identity=user.username,
                expires_delta=timedelta(hours=72),
                additional_claims=additional_claims
            )
        )

        return json_response.make_json_response(resp)

    return json_response.make_404_response('invalid combination of username and password')


@app.route('/parking')
@jwt_required()
def get_parking_by_organization():
    # read org_id param from request.args (dict)
    org_id = int(request.args.get('org_id'))
    return json_response.make_json_response(db.get_parking(org_id, mysql.connection))


@app.route('/parking/<parking_id>/places')
@jwt_required()
def get_parking_places(parking_id):
    return json_response.make_json_response(db.get_parking_places(parking_id, mysql.connection))


@app.route('/booking/my')
@jwt_required()
def get_user_bookings():
    return json_response.make_json_response(db.get_user_bookings(get_jwt()['user_id'], mysql.connection))


@app.route('/booking', methods=['POST'])
@jwt_required()
def create_booking():
    req = get_request_data(m.ParkingBooking)
    return json_response.make_json_response(db.create_booking(req, mysql.connection))


@app.route('/booking/<booking_id>', methods=['PUT', 'DELETE'])
@jwt_required()
def delete_edit_booking(booking_id):
    if request.method == 'PUT':
        req = get_request_data(m.ParkingBooking)
        return json_response.make_json_response(db.edit_booking(req, mysql.connection))
    else:
        db.delete_booking(booking_id, mysql.connection)
        return make_response('', 200)


@app.route('/parking/search', methods=['POST'])
@jwt_required()
def get_parking_by_date():
    req = get_request_data(m.GetPlacesByDateRequest)
    return json_response.make_json_response(db.get_booking_by_date(req, get_jwt(), mysql.connection))


@app.route('/check_licence', methods=['POST'])
def check_licence_plate_number():
    org_id = int(request.args.get('org_id'))
    photo = request.files['photo']
    image = Image.open(photo)
    image = np.array(image)

    licence_number = plate_number_recognition(image)
    if licence_number == 'NOT REC':
        return json_response.make_json_response(
            {
                'access': 'DENIED',
                'licence_number': licence_number
            })
    else:
        approved = db.check_number_in_db(licence_number.replace(' ', ''), org_id, mysql.connection)
        return json_response.make_json_response(
            {
                'access': 'APPROVED' if approved else 'DENIED',
                'licence_number': licence_number
            })


if __name__ == '__main__':
    with app.app_context():
        db.setup_database(mysql.connection)
    app.run(debug=True, ssl_context=('certificates/cert.pem', 'certificates/key.pem'))
