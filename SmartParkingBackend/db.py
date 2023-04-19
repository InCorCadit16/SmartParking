import models as m
import encrypt as enc

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
            FOREIGN KEY (place_id) REFERENCES Parking(id),
            FOREIGN KEY (user_id) REFERENCES UserAccount(id)
        );
        ''')

        connection.commit()


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
        ('TestUser', 'Ion', 'Bostan', enc.encrypt_password('ease1234'), '060555355', 1),
        ('TestUser2', 'Jame', 'Adams', enc.encrypt_password('password1234'), '079223322', 1)
    ]

    with connection.cursor() as cursor:
        cursor.executemany(insert_query, users)
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

