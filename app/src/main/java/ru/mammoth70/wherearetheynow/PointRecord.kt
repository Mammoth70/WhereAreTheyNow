package ru.mammoth70.wherearetheynow

import java.util.Date
import java.util.Locale

data class PointRecord (
    // Класс - record для передачи телефона, координат и времени.
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val datetime: String = "")
{
    companion object {
        const val FORMAT_DOUBLE: String = "%1$.6f"
        const val FORMAT_POINT: String = "%1$.6f %2$.6f"
        const val FORMAT_DATE: String = "%1\$tF %1\$tT"
    }

    internal constructor(
        startphone: String,
        setlatitude: Double,
        setlongitude: Double,
        setdatetime: Date
    ) : this(
        startphone,
        setlatitude,
        setlongitude,
        String.format(Locale.US, FORMAT_DATE, setdatetime)
    )

}