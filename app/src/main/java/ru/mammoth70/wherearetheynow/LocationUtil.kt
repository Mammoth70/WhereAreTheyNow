package ru.mammoth70.wherearetheynow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.SmsManager
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

// Утилиты для работы с геолокацией.


const val WAY_SMS = 1
const val WAY_LOCAL = 2
const val WAY_INTERNET = 3
private const val FORMAT_ANSWER = $$"WATN A lat %1$.6f, lon %2$.6f, time %3$s"
private const val FORMAT_REQUEST_AND_LOCATION = $$"WATN R lat %1$.6f, lon %2$.6f, time %3$s"
private const val FORMAT_JSON_LOCATION = $$"{\"latitude\": %1$.6f, \"longitude\": %2$.6f, \"battery_level\": %3$d }"
private val REGEXP_ISO_Z_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$")

fun getAndSendLocationAsync(context: Context, way: Int, address: String?, sendRequest: Boolean,
                            onFinished: (() -> Unit)? = null,
                            onResult: ((Result<String>) -> Unit)? = null
){
    // Функция асинхронно запрашивает геолокацию (если есть разрешения),
    // и отправляет ответ указанным способом.

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
    {
        onFinished?.invoke()
        return
    }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location: Location? ->
            location?.let {
                updateLocalLocation(location)
                when (way) {
                    WAY_SMS -> {
                        sendLocationSMS(context, location, address, sendRequest)
                        onFinished?.invoke()
                    }

                    WAY_LOCAL -> {
                        sendLocationLocal(context, location)
                        onFinished?.invoke()
                    }

                    WAY_INTERNET -> {
                        sendLocationInternetAsync(context, location, onFinished, onResult)
                    }
                }
            }?: run {
                onFinished?.invoke() // Если локация null
            }
        }
        .addOnFailureListener {
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


fun updateLocalLocation(location: Location) {
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

private fun sendLocationLocal(context: Context, location: Location) {
    // Функция открывает activity с картой.

    val record = PointRecord(
        DataRepository.myPhone,
        location.latitude,
        location.longitude,
        Date(location.time)
    )
    viewLocation(context, record, false)
}


private fun sendLocationSMS(
    context: Context,
    location: Location?,
    smsTo: String?,
    sendRequest: Boolean
) {
    // Функция форматирует и отправляет SMS-сообщение с координатами и временем выполнения.

    formatSmsLocation(location, sendRequest)?.let {
        val smsManager = context.getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(
            smsTo, null,
            it, null, null
        )
    }
}

fun sendLocationInternetAsync(
    context: Context,
    location: Location,
    onFinished: (() -> Unit)?,
    onResult: ((Result<String>) -> Unit)?
) {
    // Функция асинхронно форматирует и отправляет на интернет-сервер json с координатами и зарядом батареи.

    if ((!SettingsManager.useInternet) || SettingsManager.InternetServer.isBlank() || SettingsManager.InternetToken.isBlank()) {
        onResult?.invoke(Result.failure(Exception("No internet settings")))
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


fun parseAndWriteLocations(json: String) {
    // Функция парсит JSON-строку со списком координат
    // и обновляет данные в DataRepository.
    try {
        val rootObject = JSONObject(json)

        val status = rootObject.optString("status")
        if (status != "success") return

        val dataArray = rootObject.optJSONArray("data") ?: return

        for (i in 0 until dataArray.length()) {
            val deviceObject = dataArray.getJSONObject(i)

            val validRecord = checkJsonPointRecord(
                deviceObject.optString("phone").trim(),
                deviceObject.optString("latitude").trim(),
                deviceObject.optString("longitude").trim(),
                deviceObject.optString("created_at").trim())

            if (validRecord != null) {
                DataRepository.writeLastPoint(validRecord, alwaysWrite = false)
            }
        }
    } catch (e: Exception) {
        LogSmart.e("LocationUtil", "Exception в parseAndWriteLocations($json)", e)
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun checkJsonPointRecord(phone: String?, latitude: String?, longitude: String?, dateTime: String?): PointRecord? {
    // Фунция разбирает строковые поля и возвращает в случае удачи PointRecord, иначе null.
    // (поскольку данные приходят извне, проверять надо тщательно)

    return try {
        if (phone.isNullOrBlank()) return null

        val lat = latitude?.toDoubleOrNull() ?: return null
        val lon = longitude?.toDoubleOrNull() ?: return null

        if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return null

        if (dateTime.isNullOrBlank()) return null
        val matcher = REGEXP_ISO_Z_DATE.matcher(dateTime)
        if (!matcher.matches()) return null
        Instant.parse(dateTime)
        val cleanUtcDateTime = dateTime.substring(0, 19).replace('T', ' ')

        PointRecord(phone.trim(), lat, lon, cleanUtcDateTime)

    } catch (e: Exception) {
        LogSmart.e("LocationUtil", "Exception в checkRecord( $phone, $latitude, $longitude, $dateTime)", e)
        null
    }
}


fun getLocationsInternetAsync(
    onFinished: (() -> Unit)?,
    onResult: ((Result<String>) -> Unit)?
) {
    // Функция асинхронно получает с интернет-сервера и записывает в БД координаты и заряд батареи.

    if ((!SettingsManager.useInternet) || SettingsManager.InternetServer.isBlank() || SettingsManager.InternetToken.isBlank()) {
        onResult?.invoke(Result.failure(Exception("No internet settings")))
        onFinished?.invoke()
        return
    }

    NetworkManager.fetchJsonAsync(
        url = "https://${SettingsManager.InternetServer}/get_locations",
        token = SettingsManager.InternetToken,
        requestMethod = NetworkManager.HttpMethod.GET,
        onFinished = onFinished,
        onResult = { result ->
            result.onSuccess { json ->
                parseAndWriteLocations(json)
            }
            onResult?.invoke(result)
        }
    )
}