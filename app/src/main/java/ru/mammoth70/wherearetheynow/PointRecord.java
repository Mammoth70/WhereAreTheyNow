package ru.mammoth70.wherearetheynow;

import java.util.Date;
import java.util.Locale;

public class PointRecord {
    // Класс - record для передачи телефона, координат и времени.
    public static final String FORMAT_DOUBLE = "%1$.6f";
    public static final String FORMAT_POINT = "%1$.6f %2$.6f";
    public static final String FORMAT_DATE = "%1$tF %1$tT";

    public String phone;
    public double latitude;
    public double longitude;
    public String datetime;

    PointRecord(String startphone, double setlatitude, double setlongitude, String setdatetime) {
        super();
        phone = startphone;
        latitude = setlatitude;
        longitude = setlongitude;
        datetime = setdatetime;
    }

    PointRecord(String startphone, double setlatitude, double setlongitude, Date setdatetime) {
        super();
        phone = startphone;
        latitude = setlatitude;
        longitude = setlongitude;
        datetime = String.format(Locale.US, FORMAT_DATE, setdatetime);
    }

}