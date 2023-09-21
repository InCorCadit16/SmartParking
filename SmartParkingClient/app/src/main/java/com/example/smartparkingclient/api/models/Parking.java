package com.example.smartparkingclient.api.models;

import java.io.Serializable;

public class Parking extends BaseModel implements Serializable {
    public String name;
    public int placesNumber;
    public int organizationId;
}
