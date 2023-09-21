package com.example.smartparkingclient.api.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    public static String getDate(Date date) {
        var formatter = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
        return formatter.format(date);
    }

    public static String getTimeSpan(Date start, Date end) {
        var formatter = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        return formatter.format(start) + '-' + formatter.format(end);
    }
}
