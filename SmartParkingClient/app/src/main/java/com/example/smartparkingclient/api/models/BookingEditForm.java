package com.example.smartparkingclient.api.models;

import java.io.Serializable;
import java.util.Date;

public class BookingEditForm implements Serializable {
    public int id;
    public Date startDate;
    public Date endDate;
    public int parkingId;
    public int placeId;
}
