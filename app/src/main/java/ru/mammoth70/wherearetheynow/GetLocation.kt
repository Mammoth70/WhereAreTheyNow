package ru.mammoth70.wherearetheynow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.SmsManager
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Date
import java.util.Locale

class GetLocation {
    // Класс запрашивает геолокацию и возвращает её указанным способом.


    companion object {
        const val WAY_SMS = 1
        const val WAY_LOCAL = 2
        private const val FORMAT_ANSWER = $$"WATN A lat %1$.6f, lon %2$.6f, time %3$tF %3$tT"
        private const val FORMAT_REQUEST_AND_LOCATION = $$"WATN R lat %1$.6f, lon %2$.6f, time %3$tF %3$tT"
    }


    fun sendLocation(context: Context, way: Int, address: String?, sendRequest: Boolean, onFinished: (() -> Unit)? = null) {
        // Функция запрашивает геолокацию (если есть разрешения),
        // и отправляет ответ указанным способом.

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if ((ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
            (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        ) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        updateLocalLocation(location)
                        when (way) {
                            WAY_SMS -> sendSMS(context, location, address, sendRequest)
                            WAY_LOCAL -> sendLocal(context, location)
                        }
                    }
                    onFinished?.invoke()
                }
                .addOnFailureListener {
                    onFinished?.invoke()
                }
        } else {
            onFinished?.invoke()
        }
    }


    private fun formatLocation(location: Location?, sendRequest: Boolean): String? {
        // Функция форматирует геолокацию для SMS-сообщения.

        location ?: return null
        return formatLocation(location.latitude, location.longitude, Date(location.time), sendRequest)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun formatLocation(latitude: Double, longitude: Double, date: Date, sendRequest: Boolean): String? {
        // Функция форматирует для SMS-сообщения геолокацию, разбитую по отдельным полям.

        return if (sendRequest) {
            String.format(Locale.US, FORMAT_REQUEST_AND_LOCATION, latitude, longitude, date)
        } else {
            String.format(Locale.US, FORMAT_ANSWER, latitude, longitude, date)
        }
    }


    private fun updateLocalLocation(location: Location) {
        // Функция сохраняет локальное состояние локации.

        if (DataRepository.myPhone.isEmpty()) {
            return
        }
        val record = PointRecord(
            DataRepository.myPhone,
            location.latitude,
            location.longitude,
            Date(location.time)
        )
        DataRepository.writeLastPoint(record)
    }


    private fun sendSMS(
        context: Context,
        location: Location?,
        smsTo: String?,
        sendRequest: Boolean
    ) {
        // Функция отправляет SMS-сообщение.

        formatLocation(location, sendRequest)?.let {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(smsTo, null,
                it, null, null)
        }
    }


    private fun sendLocal(context: Context, location: Location) {
        // Функция открывает activity с картой.

        val record = PointRecord(
            DataRepository.myPhone,
            location.latitude,
            location.longitude,
            Date(location.time)
        )
        viewLocation(context, record, false)
    }

}