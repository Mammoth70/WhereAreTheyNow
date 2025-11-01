package ru.mammoth70.wherearetheynow

import java.util.Date
import java.util.Locale

data class PointRecord (
    // data класс - для хранения телефона, координат и времени получения координат.
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val dateTime: String = "")
{
    companion object {
        const val FORMAT_DOUBLE: String = "%1$.6f"
        const val FORMAT_POINT: String = "%1$.6f %2$.6f"
        const val FORMAT_DATE: String = $$"%1$tF %1$tT"
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
        String.format(Locale.US, FORMAT_DATE, initDateTime)
    )

}