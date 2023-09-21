package com.example.smartparkingclient.api.contracts;

import com.example.smartparkingclient.api.models.Parking;
import com.example.smartparkingclient.api.models.ParkingPlace;

public class GetPlacesByDateResponse {
    public Parking[] parkings;
    public ParkingPlace[] places;
}
