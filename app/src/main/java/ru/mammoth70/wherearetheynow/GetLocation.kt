package ru.mammoth70.wherearetheynow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.OnSuccessListener
import java.util.Date
import java.util.Locale
import ru.mammoth70.wherearetheynow.Util.FORMAT_REQUEST_AND_LOCATION
import ru.mammoth70.wherearetheynow.Util.FORMAT_ANSWER

class GetLocation {
    // Класс запрашивает геолокацию и возвращает её указанным способом.

    companion object {
        const val WAY_SMS: Int = 1
        const val WAY_LOCAL: Int = 2
    }

    fun sendLocation(context: Context, way: Int, address: String?, sendRequest: Boolean) {
        // Функция запрашивает геолокацию (если есть разрешения),
        // и отправляет ответ указанным способом.
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if ((ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) &&
            (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(OnSuccessListener { location: Location? ->
                    location?.let {
                        updateLocalLocation(location)
                        when (way) {
                            WAY_SMS -> sendSMS(context, location, address, sendRequest)
                            WAY_LOCAL -> sendLocal(context, location)
                        }
                    }
                })
        }
    }

    private fun formatLocation(location: Location?, sendRequest: Boolean): String? {
        // Функция форматирует геолокацию для SMS-сообщения.
        location ?: return null
        return if (sendRequest) {
            String.format(
                Locale.US, FORMAT_REQUEST_AND_LOCATION,
                location.latitude, location.longitude, Date(location.time)
            )
        } else {
            String.format(
                Locale.US, FORMAT_ANSWER,
                location.latitude, location.longitude, Date(location.time)
            )
        }
    }

    private fun updateLocalLocation(location: Location) {
        // Функция сохраняет локальное состояние локации.
        if (Util.myphone.isEmpty()) {
            return
        }
        val record = PointRecord(
            Util.myphone,
            location.latitude,
            location.longitude,
            Date(location.time)
        )
        DBhelper.dbHelper.writeLastPoint(record)
    }

    private fun sendSMS(
        context: Context,
        location: Location?,
        smsTo: String?,
        sendRequest: Boolean
    ) {
        // Функция отправляет SMS-сообщение.
        val message = formatLocation(location, sendRequest)
        message?.let {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(smsTo, null,
                message, null, null)
        }
    }

    private fun sendLocal(context: Context, location: Location) {
        // Функция открывает activity с картой.
        val record = PointRecord(
            Util.myphone,
            location.latitude,
            location.longitude,
            Date(location.time)
        )
        MapUtil.viewLocation(context, record, false)
    }

}