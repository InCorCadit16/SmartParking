import models as m
import encrypt as enc
import typing as t
import pydantic as p
from shapely.geometry import Point

DB_NAME = 'smartparking'


def setup_database(connection):
    with connection.cursor() as cursor:
        need_seed = not database_exists(connection)

        cursor.execute(f'CREATE DATABASE IF NOT EXISTS {DB_NAME}')
        cursor.execute(f'USE {DB_NAME}')

        if need_seed:
            create_schema(connection)
            add_salt_data(connection)


def database_exists(connection):
    with connection.cursor() as cursor:
        cursor.execute("SHOW DATABASES")
        rows = cursor.fetchall()
        for row in rows:
            if row[0] == DB_NAME:
                return True
        return False


def create_schema(connection):
    with connection.cursor() as cursor:
        cursor.execute('''
        CREATE TABLE Organization (
            id INT PRIMARY KEY AUTO_INCREMENT,
            name VARCHAR(50) NOT NULL UNIQUE,
            address VARCHAR(100) NOT NULL
        );
        ''')

        cursor.execute('''
        CREATE TABLE LicencePlate (
            id INT PRIMARY KEY AUTO_INCREMENT,
            licence_number VARCHAR(50) NOT NULL UNIQUE,
            organization_id INT,
            FOREIGN KEY (organization_id) REFERENCES Organization(id)
        )
        ''')

        cursor.execute('''
        CREATE TABLE UserAccount (
            id INT PRIMARY KEY AUTO_INCREMENT,
            username VARCHAR(50) UNIQUE NOT NULL,
            first_name VARCHAR(50) NOT NULL,
            last_name VARCHAR(50) NOT NULL,
            password VARCHAR(300) NOT NULL,
            phone VARCHAR(15) NOT NULL,
            organization_id INT,
            FOREIGN KEY (organization_id) REFERENCES Organization(id)
        );
        ''')

        cursor.execute('''
        CREATE TABLE Parking (
            id INT PRIMARY KEY AUTO_INCREMENT,
            name VARCHAR(50) NOT NULL,
            organization_id INT,
            FOREIGN KEY (organization_id) REFERENCES Organization(id)
        );
        ''')

        cursor.execute('''
        CREATE TABLE ParkingPlace (
            id INT PRIMARY KEY AUTO_INCREMENT,
            code VARCHAR(8),
            location POINT NOT NULL,
            parking_id INT,
            FOREIGN KEY (parking_id) REFERENCES Parking(id)
        );
        ''')

        cursor.execute('''
        CREATE TABLE ParkingBooking (
            id INT PRIMARY KEY AUTO_INCREMENT,
            start_date DATETIME NOT NULL,
            end_date DATETIME NOT NULL,
            user_id INT,
            place_id INT,
            FOREIGN KEY (place_id) REFERENCES ParkingPlace(id),
            FOREIGN KEY (user_id) REFERENCES UserAccount(id)
        );
        ''')

        connection.commit()

        # cursor.execute('''
        #         DELIMITER //
        #         CREATE TRIGGER delete_expired_bookings
        #         AFTER INSERT ON ParkingBooking
        #         FOR EACH ROW
        #         BEGIN
        #           IF ParkingBooking.end_date < NOW() THEN
        #             DELETE FROM ParkingBooking WHERE id = ParkingBooking.id;
        #           END IF;
        #         END //
        #         DELIMITER ;
        # ''')
        #
        # cursor.execute('SET GLOBAL event_scheduler = ON;')
        #
        # cursor.execute('''
        #         CREATE EVENT delete_expired_event
        #         ON SCHEDULE EVERY 15 MINUTE
        #         DO
        #           DELETE FROM ParkingBooking WHERE end_date < NOW();
        # ''')
        #
        # connection.commit()


def add_salt_data(connection):
    with connection.cursor() as cursor:
        cursor.execute('''
            INSERT INTO organization (name, address) VALUES ("UTM", "Moldova, Chisinau, Str. Studentilor 9");
        ''')
        connection.commit()

    insert_query = '''
        INSERT INTO useraccount (username, first_name, last_name, password, phone, organization_id)
        VALUES (%s, %s, %s, %s, %s, %s);
    '''

    users = [
        ('TestUser', 'John', 'Doe', enc.encrypt_password('12sample34'), '060555355', 1),
        ('TestUser2', 'Jame', 'Adams', enc.encrypt_password('password1234'), '079223322', 1)
    ]

    with connection.cursor() as cursor:
        cursor.executemany(insert_query, users)
        connection.commit()

    with connection.cursor() as cursor:
        cursor.execute('''
            INSERT INTO LicencePlate (licence_number, organization_id) VALUES 
            ('AAC931', 1), ('GXO526', 1), ('BZY473', 1);
        ''')
        connection.commit()

    with connection.cursor() as cursor:
        parkings = [
            ('Central Parking', 1),
            ('Downtown Parking', 1),
            ('City Center Parking', 1)
        ]
        parking_query = "INSERT INTO Parking (name, organization_id) VALUES (%s, %s)"
        cursor.executemany(parking_query, parkings)
        connection.commit()

    with connection.cursor() as cursor:
        parking_places = [
            ('A1', Point(40.7128, -74.0060).wkt, 1),
            ('A2', Point(45.7128, -79.0060).wkt, 1),
            ('A3', Point(50.7128, -84.0060).wkt, 1),
            ('A4', Point(55.7128, -89.0060).wkt, 1),
            ('A5', Point(60.7128, -94.0060).wkt, 1),
            ('B1', Point(37.7749, -122.4194).wkt, 2),
            ('B2', Point(42.7749, -117.4194).wkt, 2),
            ('B3', Point(47.7749, -112.4194).wkt, 2),
            ('B4', Point(47.7749, -112.4194).wkt, 2),
            ('B5', Point(47.7749, -112.4194).wkt, 2),
            ('B6', Point(47.7749, -112.4194).wkt, 2),
            ('C1', Point(51.5074, -0.1278).wkt, 3),
            ('C2', Point(56.5074, -5.1278).wkt, 3),
            ('C2', Point(56.5074, -5.1278).wkt, 3),
            ('C3', Point(56.5074, -5.1278).wkt, 3),
            ('C4', Point(56.5074, -5.1278).wkt, 3),
            ('C5', Point(61.5074, -10.1278).wkt, 3)
        ]
        parking_place_query = "INSERT INTO ParkingPlace (code, location, parking_id) VALUES (%s, ST_GeomFromText(%s), %s)"
        cursor.executemany(parking_place_query, parking_places)
        connection.commit()

    with connection.cursor() as cursor:
        parking_bookings = [
            (1, '2023-06-12 12:00:00', '2023-06-12 17:00:00', 1),
            (3, '2023-06-13 13:00:00', '2023-06-13 17:00:00', 2),
            (6, '2023-06-15 11:00:00', '2023-06-15 18:00:00', 1),
            (4, '2023-06-16 09:00:00', '2023-06-16 18:00:00', 1),
            (9, '2023-06-20 14:30:00', '2023-06-20 19:30:00', 1),
            (5, '2023-06-21 11:00:00', '2023-06-21 18:00:00', 1)
        ]
        parking_booking_query = \
            "INSERT INTO ParkingBooking (place_id, start_date, end_date, user_id) VALUES (%s, %s, %s, %s)"
        cursor.executemany(parking_booking_query, parking_bookings)
        connection.commit()


def login_user(req: m.LoginRequest, connection) -> m.User:
    with connection.cursor() as cursor:
        cursor.execute(f'SELECT id, username, password from useraccount')
        for user in cursor:
            if req.username == user[1] and enc.check_password(req.password, user[2]):
                cursor.execute(f'SELECT * from useraccount WHERE id = {user[0]}')
                data = cursor.fetchall()[0]
                return m.User(
                    id=data[0],
                    username=data[1],
                    firstName=data[2],
                    lastName=data[3],
                    phone=data[5],
                    organizationId=data[6]
                )


def get_parking(org_id: int, connection) -> t.List[m.Parking]:
    with connection.cursor() as cursor:
        subreq = '(SELECT COUNT(*) from parkingplace where parking_id=parking.id) AS placesNumber'
        cursor.execute(f'SELECT parking.id, parking.name, parking.organization_id, {subreq} from parking where organization_id={org_id}')
        return map_cursor_to_list(cursor, m.Parking)


def get_parking_places(park_id: int, connection) -> m.GetPlacesResponse:
    with connection.cursor() as cursor:
        cursor.execute(f'SELECT id, code, ST_AsText(location), parking_id from parkingplace where parking_id={park_id}')
        places = map_cursor_to_list(cursor, m.ParkingPlace)
        return m.GetPlacesResponse(parkingId=park_id, places=places)


def get_user_bookings(user_id: int, connection) -> m.UserBookingsResponse:
    with connection.cursor() as cursor:
        cursor.execute(f'SELECT * from parkingbooking where user_id={user_id}')
        bookings = map_cursor_to_list(cursor, m.ParkingBooking)

        places_ids = ', '.join(list(map(lambda i: str(i.placeId), bookings)))
        cursor.execute(f'SELECT id, code, ST_AsText(location), parking_id from parkingplace where id IN ({places_ids})')
        places = map_cursor_to_list(cursor, m.ParkingPlace)

        subreq = '(SELECT COUNT(*) from parkingplace where parking_id=parking.id) AS placesNumber'
        parking_ids = ', '.join(list(map(lambda i: str(i.parkingId), places)))
        cursor.execute(f'SELECT parking.id, parking.name, parking.organization_id, {subreq} from parking where id IN ({parking_ids})')
        parkings = map_cursor_to_list(cursor, m.Parking)

        return m.UserBookingsResponse(
            bookings=bookings,
            places=places,
            parkings=parkings
        )


def create_booking(req: m.ParkingBooking, connection) -> m.ParkingBooking:
    with connection.cursor() as cursor:
        value = (req.startDate, req.endDate, req.userId, req.placeId)
        cursor.execute(f"INSERT INTO parkingbooking(start_date, end_date, user_id, place_id) VALUES (%s, %s, %s, %s)", value)
        connection.commit()
        cursor.execute(f'SELECT * from parkingbooking where id={cursor.lastrowid}')
        return map_cursor_item_to_model(cursor.fetchall()[0], m.ParkingBooking)


def edit_booking(req: m.ParkingBooking, connection) -> m.ParkingBooking:
    with connection.cursor() as cursor:
        values = (req.startDate, req.endDate, req.userId, req.placeId, req.id)
        cursor.execute(
            f'UPDATE parkingbooking SET start_date=%s, end_date=%s, user_id=%s, place_id=%s WHERE id=%s', values)
        connection.commit()
        cursor.execute(f'SELECT * from parkingbooking where id={req.id}')
        return map_cursor_item_to_model(cursor.fetchall()[0], m.ParkingBooking)


def delete_booking(booking_id: int, connection):
    with connection.cursor() as cursor:
        cursor.execute(f'DELETE FROM parkingbooking WHERE id={booking_id}')
        connection.commit()


def get_booking_by_date(req: m.GetPlacesByDateRequest, user_data, connection) -> m.GetPlacesByDateResponse:
    parkings = get_parking(user_data['organization_id'], connection)
    with connection.cursor() as cursor:
        parking_ids = ', '.join(list(map(lambda i: str(i.id), parkings)))
        cursor.execute(f'SELECT id, code, ST_AsText(location), parking_id from parkingplace where parking_id IN ({parking_ids})')
        places = map_cursor_to_list(cursor, m.ParkingPlace)

        cursor.execute(f"SELECT * from parkingbooking where NOT (start_date < '{req.startDate}' OR end_date > '{req.endDate}')")
        bookings = map_cursor_to_list(cursor, m.ParkingBooking)
        bookings = [bk for bk in bookings if bk.userId != user_data['user_id']]
        booked_places_ids = set(map(lambda b: b.placeId, bookings))
        places = [pl for pl in places if pl.id not in booked_places_ids]
        parkings_ids = set(map(lambda p: p.parkingId, places))
        parkings = [pk for pk in parkings if pk.id in parkings_ids]

    return m.GetPlacesByDateResponse(
        parkings=parkings,
        places=places,
    )


def check_number_in_db(licence_number: str, org_id: int, connection) -> bool:
    with connection.cursor() as cursor:
        cursor.execute(f'SELECT licence_number from LicencePlate WHERE organization_id={org_id};')
        return licence_number in list(map(lambda tp: tp[0], cursor.fetchall()))


def map_cursor_to_list(cursor, model_type: t.Type[p.BaseModel]) -> t.Any:
    models = []
    for c in cursor:
        models.append(map_cursor_item_to_model(c, model_type))
    return models


def map_cursor_item_to_model(cursor_item, model_type: t.Type[p.BaseModel]) -> t.Any:
    model_dict = dict(zip(model_type.__fields__.keys(), cursor_item))
    model = model_type(**model_dict)
    return model
