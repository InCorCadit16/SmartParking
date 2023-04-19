import bcrypt

salt = bcrypt.gensalt()


def encrypt_password(password):
    return bcrypt.hashpw(password.encode('utf-8'), salt)


def check_password(password, encrypted_password):
    return bcrypt.checkpw(password.encode('utf-8'), encrypted_password.encode('utf-8'))
