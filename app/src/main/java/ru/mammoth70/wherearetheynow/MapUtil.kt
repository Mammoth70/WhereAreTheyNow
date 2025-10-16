package ru.mammoth70.wherearetheynow

import android.content.Context
import android.content.Intent
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP_ZOOM
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP_TILT
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP_CIRCLE
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP_CIRCLE_RADIUS
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_SMS_FROM
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_LATITUDE
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_LONGITUDE
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_TIME

object MapUtil {
    // Объект содержит настройки карт и утилиты работы с данными карт.

    const val MAP_TEXT: Int = 0
    const val MAP_YANDEX: Int = 1
    const val MAP_OPENSTREET: Int = 2
    const val MAP_DEFAULT: Int = MAP_YANDEX
    const val NAME_MAP: String = "map"
    const val NAME_MAP_ZOOM: String = "zoom"
    const val NAME_MAP_TILT: String = "tilt"
    const val NAME_MAP_CIRCLE: String = "circle"
    const val NAME_MAP_CIRCLE_RADIUS: String = "radius"

    const val MAP_ZOOM_DEFAULT: Float = 17f
    const val MAP_TILT_DEFAULT: Float = 30f
    const val MAP_CIRCLE_DEFAULT: Boolean = true
    const val MAP_CIRCLE_DEFAULT_RADIUS: Float = 70f

    var selectedMap: Int = MAP_DEFAULT
    var selectedMapZoom: Float = MAP_ZOOM_DEFAULT
    var selectedMapTilt: Float = MAP_TILT_DEFAULT
    var selectedMapCircle: Boolean = MAP_CIRCLE_DEFAULT
    var selectedMapCircleRadius: Float = MAP_CIRCLE_DEFAULT_RADIUS

    fun viewLocation(context: Context, record: PointRecord, newTask: Boolean) {
        // Функция получает данные из последней SMS,
        // проверяет их и выводит в выбранную карту.
        if ((record.latitude > -90) && (record.latitude < 90) &&
            (record.longitude > -180) && (record.longitude < 180) &&
            (record.phone in Util.phones)
        ) {
            val intent: Intent
            when (selectedMap) {
                MAP_YANDEX -> {
                    intent = Intent(context, YandexActivity::class.java)
                    intent.putExtra(INTENT_EXTRA_MAP_ZOOM,
                        selectedMapZoom)
                    intent.putExtra(INTENT_EXTRA_MAP_TILT,
                        selectedMapTilt)
                    intent.putExtra(INTENT_EXTRA_MAP_CIRCLE,
                        selectedMapCircle)
                    intent.putExtra(INTENT_EXTRA_MAP_CIRCLE_RADIUS,
                        selectedMapCircleRadius)
                }
                MAP_OPENSTREET -> {
                    intent = Intent(context, BrowserActivity::class.java)
                    intent.putExtra(INTENT_EXTRA_MAP,
                        selectedMap)
                    intent.putExtra( INTENT_EXTRA_MAP_ZOOM,
                        selectedMapZoom)
                }
                else -> intent = Intent(context, TextActivity::class.java)
            }
            if (newTask) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.putExtra(INTENT_EXTRA_SMS_FROM, record.phone)
            intent.putExtra(INTENT_EXTRA_LATITUDE, record.latitude)
            intent.putExtra(INTENT_EXTRA_LONGITUDE, record.longitude)
            intent.putExtra(INTENT_EXTRA_TIME, record.dateTime)
            context.startActivity(intent)
        }
    }

    fun timePassed(dateTime: String, context: Context): String {
        // Функция возвращает разницу в минутах между текущим временем
        // и временем в пришедшем SMS-сообщении.
        val dateCurrent = Date()
        val dateSMS = Util.stringToDate(dateTime)
        if (dateSMS == null) {
            return ""
        }
        val duration = dateCurrent.time - dateSMS.time
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        return if (diffInMinutes < 1) {
            context.getString(R.string.now)
        } else if (diffInMinutes in 1..30) {
            String.format(Locale.US, context.getString(R.string.minutes_ago), diffInMinutes)
        } else {
            context.getString(R.string.long_ago)
        }
    }

}