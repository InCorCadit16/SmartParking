import pydantic as p
import typing as t
from datetime import datetime


class Model(p.BaseModel):
    id: t.Optional[int]


class User(Model):
    username: str
    firstName: str
    lastName: str
    phone: str
    organizationId: int


class ParkingPlace(Model):
    code: str
    location: str
    parkingId: int


class Parking(Model):
    name: str
    organizationId: int
    placesNumber: int


class ParkingBooking(Model):
    startDate: datetime
    endDate: datetime
    userId: int
    placeId: int


class LoginRequest(p.BaseModel):
    username: str
    password: str


class LoginResponse(p.BaseModel):
    token: str
    user: User


class GetPlacesResponse(p.BaseModel):
    parkingId: int
    places: t.List[ParkingPlace]


class UserBookingsResponse(p.BaseModel):
    bookings: t.List[ParkingBooking]
    places: t.List[ParkingPlace]
    parkings: t.List[Parking]


class GetPlacesByDateRequest(p.BaseModel):
    startDate: datetime
    endDate: datetime


class GetPlacesByDateResponse(p.BaseModel):
    parkings: t.List[Parking]
    places: t.List[ParkingPlace]
