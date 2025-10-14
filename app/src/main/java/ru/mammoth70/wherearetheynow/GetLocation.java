package ru.mammoth70.wherearetheynow;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.telephony.SmsManager;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Date;
import java.util.Locale;

public class GetLocation {
    // Класс запрашивает геолокацию и возвращает её указанным способом.

    public final static int WAY_SMS = 1;
    public final static int WAY_LOCAL = 2;

    public void sendLocation(Context context, int way, String address, Boolean sendRequest) {
        // Метод запрашивает геолокацию (если есть разрешения), и отправляет ответ указанным способом.
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if ((ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            updateLocalLocation(context, location);
                            switch (way) {
                                case WAY_SMS:
                                    sendSMS(context,location, address, sendRequest);
                                    break;
                                case WAY_LOCAL:
                                    sendLocal(context, location);
                                    break;
                            }

                        }
                    });
        }
    }

    private String formatLocation(Location location, Boolean sendRequest) {
        // Метод форматирует геолокацию для SMS-сообщения.
        if (location == null)
            return null;
        if (sendRequest) {
            return String.format(Locale.US, Util.FORMAT_REQUEST_AND_LOCATION,
                    location.getLatitude(), location.getLongitude(), new Date(location.getTime()));
        } else {
            return String.format(Locale.US, Util.FORMAT_ANSWER,
                    location.getLatitude(), location.getLongitude(), new Date(location.getTime()));
        }
    }

    private void updateLocalLocation(Context context, Location location) {
        // Метод сохраняет локальное состояние локации данные в HashMap, в SharedPreferences и в БД.
        if ((Util.myphone != null) && (!Util.myphone.isEmpty())) {
            PointRecord record = new PointRecord(
                    Util.myphone,
                    location.getLatitude(),
                    location.getLongitude(),
                    new Date(location.getTime()));
            MapUtil.setLastAnswer(context, record);
        }
    }

    private void sendSMS(Context context, Location location, String smsTo, Boolean sendRequest) {
        // Метод отправляет SMS-сообщение.
        String message = formatLocation(location, sendRequest);
        if (message != null) {
            SmsManager smsManager = context.getSystemService(SmsManager.class);
            smsManager.sendTextMessage(smsTo, null, message, null, null);
        }
    }

    private void sendLocal(Context context, Location location) {
        // Метод открывает activity с картой.
        PointRecord record = new PointRecord(
                Util.myphone,
                location.getLatitude(),
                location.getLongitude(),
                new Date(location.getTime()));
        MapUtil.viewLocation(context, record, false);
    }

}