package com.example.smartparkingclient.api.models;

import java.io.Serializable;
import java.util.Date;

public class ParkingBooking extends BaseModel implements Serializable {
    public Date startDate;
    public Date endDate;
    public int userId;
    public int placeId;
}
