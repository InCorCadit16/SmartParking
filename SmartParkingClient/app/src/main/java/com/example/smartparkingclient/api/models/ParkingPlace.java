package com.example.smartparkingclient.api.models;

import java.io.Serializable;

public class ParkingPlace extends BaseModel implements Serializable {
    public String code;
    public String location;
    public int parkingId;
}
