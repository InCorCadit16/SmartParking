import typing as t
import pydantic as p

from flask import Flask, request
from flask_jwt_extended import JWTManager, jwt_required, create_access_token
from flask_mysqldb import MySQL

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
            authToken=create_access_token(identity=user.username, additional_claims=additional_claims)
        )

        return json_response.make_json_response(resp)

    return json_response.make_404_response('invalid combination of username and password')


@app.route('/parking')
@jwt_required()
def get_parking_for_user():
    return json_response.make_json_response({'result': 'success'})


if __name__ == '__main__':
    with app.app_context():
        db.setup_database(mysql.connection)
    app.run(debug=True, ssl_context=('certificates/cert.pem', 'certificates/key.pem'))

