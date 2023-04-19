import pydantic as p


class User(p.BaseModel):
    id: int
    username: str
    firstName: str
    lastName: str
    phone: str
    organizationId: int


class LoginRequest(p.BaseModel):
    username: str
    password: str


class LoginResponse(p.BaseModel):
    authToken: str
    user: User
