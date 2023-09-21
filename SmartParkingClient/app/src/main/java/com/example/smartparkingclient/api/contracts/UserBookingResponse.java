package com.example.smartparkingclient.api.contracts;

import com.example.smartparkingclient.api.models.Parking;
import com.example.smartparkingclient.api.models.ParkingBooking;
import com.example.smartparkingclient.api.models.ParkingPlace;

public class UserBookingResponse {
    public ParkingBooking[] bookings;
    public ParkingPlace[] places;
    public Parking[] parkings;

    public UserBookingResponse() {
        bookings = new ParkingBooking[]{};
        places = new ParkingPlace[]{};
        parkings = new Parking[]{};
    }
}
