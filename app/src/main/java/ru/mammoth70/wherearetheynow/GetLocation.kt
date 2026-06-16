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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GetLocation {
    // Класс запрашивает геолокацию и возвращает её указанным способом.

    companion object {
        const val WAY_SMS = 1
        const val WAY_LOCAL = 2
        const val WAY_INTERNET = 3
        private const val FORMAT_ANSWER = $$"WATN A lat %1$.6f, lon %2$.6f, time %3$s"
        private const val FORMAT_REQUEST_AND_LOCATION = $$"WATN R lat %1$.6f, lon %2$.6f, time %3$s"
        private const val FORMAT_JSON_LOCATION = $$"{\"latitude\": %1$.6f, \"longitude\": %2$.6f, \"battery_level\": %3$d }"
    }


    fun sendLocation(context: Context, way: Int, address: String?, sendRequest: Boolean,
                     onFinished: (() -> Unit)? = null,
                     onResult: ((Result<String>) -> Unit)? = null
    ){
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
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        updateLocalLocation(location)
                        when (way) {
                            WAY_SMS -> {
                                sendSMS(context, location, address, sendRequest)
                                onFinished?.invoke()
                            }
                            WAY_LOCAL -> {
                                sendLocal(context, location)
                                onFinished?.invoke()
                            }
                            WAY_INTERNET -> {
                                setLocationInternet(context, location, onFinished, onResult)
                            }
                        }
                    }?: run {
                        onFinished?.invoke() // Если локация null
                    }
                }
                .addOnFailureListener {
                    onFinished?.invoke()
                }
        } else {
            onFinished?.invoke()
        }
    }


    private fun formatSmsLocation(location: Location?, sendRequest: Boolean): String? {
        // Функция форматирует геолокацию для SMS-сообщения.

        location ?: return null
        val utcFormatter = SimpleDateFormat(FORMAT_DATETIME, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val utcDateTimeString = utcFormatter.format(Date(location.time))
        return formatSmsLocation(location.latitude, location.longitude, utcDateTimeString, sendRequest)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun formatSmsLocation(latitude: Double, longitude: Double, dateTimeStr: String, sendRequest: Boolean): String? {
        // Функция форматирует для SMS-сообщения геолокацию, разбитую по отдельным полям.

        return if (sendRequest) {
            String.format(Locale.US, FORMAT_REQUEST_AND_LOCATION, latitude, longitude, dateTimeStr)
        } else {
            String.format(Locale.US, FORMAT_ANSWER, latitude, longitude, dateTimeStr)
        }
    }


    private fun formatJsonLocation(location: Location?, battery: Int = 100): String? {
        // Функция форматирует геолокацию и заряд батареи для JSON-сообщения.

        location ?: return null
        return formatJsonLocation(location.latitude, location.longitude, battery)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun formatJsonLocation(latitude: Double, longitude: Double, battery: Int = 100): String {
        // Функция форматирует для JSON-сообщения геолокацию, разбитую по отдельным полям и заряд батареи.

        return String.format(Locale.US, FORMAT_JSON_LOCATION, latitude, longitude, battery)
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


    private fun sendSMS(
        context: Context,
        location: Location?,
        smsTo: String?,
        sendRequest: Boolean
    ) {
        // Функция отправляет SMS-сообщение.

        formatSmsLocation(location, sendRequest)?.let {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(
                smsTo, null,
                it, null, null
            )
        }
    }

    private fun setLocationInternet(
        context: Context,
        location: Location,
        onFinished: (() -> Unit)?,
        onResult: ((Result<String>) -> Unit)?
    ) {
        // Функция отправляет на интернет-сервер координаты и заряд батареи.

        if (!isInternetAvailable(context)) {
            onResult?.invoke(Result.failure(Exception("No internet connection")))
            onFinished?.invoke()
            return
        }

        val battery = getBatteryLevel(context)
        formatJsonLocation(location, battery)?.let { json ->
            NetworkManager.fetchJsonAsync(
                url = "https://${SettingsManager.InternetServer}/set_location",
                token = SettingsManager.InternetToken,
                json = json,
                onFinished = onFinished,
                onResult = onResult
            )
        } ?: run {
            onFinished?.invoke()
        }
    }

}