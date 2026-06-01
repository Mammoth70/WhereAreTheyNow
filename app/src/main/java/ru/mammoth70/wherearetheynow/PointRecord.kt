package ru.mammoth70.wherearetheynow

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class PointRecord (
    // Класс данных для хранения телефона, координат и времени получения координат.
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val dateTime: String = "",
){

    companion object {
        const val FORMAT_DOUBLE = "%1$.6f"
        const val FORMAT_POINT = "%1$.6f %2$.6f"
    }

    internal constructor(
        initPhone: String,
        initLatitude: Double,
        initLongitude: Double,
        initDateTime: Date
    ) : this(
        initPhone,
        initLatitude,
        initLongitude,
        SimpleDateFormat(FORMAT_DATETIME, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(initDateTime)
    )

}