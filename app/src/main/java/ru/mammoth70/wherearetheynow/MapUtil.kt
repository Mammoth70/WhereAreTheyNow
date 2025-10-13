package ru.mammoth70.wherearetheynow

import android.content.Context
import android.content.Intent
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

object MapUtil {
    // Класс содержит настройки карт и утилиты работы с данными карт.
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
        // Метод получает данные из последней SMS,
        // проверяет их и выводит в выбранную карту.
        if ((record.latitude > -90) && (record.latitude < 90) &&
            (record.longitude > -180) && (record.longitude < 180) &&
            (Util.phones.contains(record.phone))
        ) {
            val intent: Intent
            when (selectedMap) {
                MAP_YANDEX -> {
                    intent = Intent(context, YandexActivity::class.java)
                    intent.putExtra(Util.INTENT_EXTRA_MAP_ZOOM,
                        selectedMapZoom)
                    intent.putExtra(Util.INTENT_EXTRA_MAP_TILT,
                        selectedMapTilt)
                    intent.putExtra(Util.INTENT_EXTRA_MAP_CIRCLE,
                        selectedMapCircle)
                    intent.putExtra(Util.INTENT_EXTRA_MAP_CIRCLE_RADIUS,
                        selectedMapCircleRadius)
                }

                MAP_OPENSTREET -> {
                    intent = Intent(context, BrowserActivity::class.java)
                    intent.putExtra(Util.INTENT_EXTRA_MAP,
                        selectedMap)
                    intent.putExtra(Util.INTENT_EXTRA_MAP_ZOOM,
                        selectedMapZoom)
                }

                else -> intent = Intent(context, TextActivity::class.java)
            }
            if (newTask) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.putExtra(Util.INTENT_EXTRA_SMS_FROM, record.phone)
            intent.putExtra(Util.INTENT_EXTRA_LATITUDE, record.latitude)
            intent.putExtra(Util.INTENT_EXTRA_LONGITUDE, record.longitude)
            intent.putExtra(Util.INTENT_EXTRA_TIME, record.datetime)
            context.startActivity(intent)
        }
    }

    fun setLastAnswer(context: Context, record: PointRecord) {
        // Метод сохраняет в HashMap, SharedPreferences и в БД данные с последнего ответа на запрос.
        if ((record.latitude > -90) && (record.latitude < 90) &&
            (record.longitude > -180) && (record.longitude < 180) &&
            (Util.phones.contains(record.phone))
        ) {
            Util.phone2record.put(record.phone, record)
            val settings = context.getSharedPreferences(Util.NAME_LAST_USER,
                Context.MODE_PRIVATE)
            settings.edit {
                putString(Util.INTENT_EXTRA_SMS_FROM, record.phone)
                putString(
                    Util.INTENT_EXTRA_LATITUDE,
                    String.format(Locale.US, PointRecord.FORMAT_DOUBLE,
                        record.latitude)
                )
                putString(
                    Util.INTENT_EXTRA_LONGITUDE,
                    String.format(Locale.US, PointRecord.FORMAT_DOUBLE,
                        record.longitude)
                )
                putString(Util.INTENT_EXTRA_TIME,
                    record.datetime)
            }
            DBhelper(context).use { dBhelper ->
                dBhelper.setLastPoint(record)
            }
        }
    }

    fun getLastAnswer(context: Context): PointRecord {
        // Метод считывает из SharedPreferences данные с последнего ответа на запрос.
        val settings = context.getSharedPreferences(Util.NAME_LAST_USER,
            Context.MODE_PRIVATE)
        return PointRecord(
            settings.getString(Util.INTENT_EXTRA_SMS_FROM,
                "") ?:"",
            settings.getString(Util.INTENT_EXTRA_LATITUDE,
                "0")!!.toDouble(),
            settings.getString(Util.INTENT_EXTRA_LONGITUDE,
                "0")!!.toDouble(),
            settings.getString(Util.INTENT_EXTRA_TIME,
                "") ?:""
        )
    }

    fun timePassed(dateTime: String, context: Context): String {
        // Метод возвращает разницу в минутах между текущим временем
        // и временем в пришедшем SMS-сообщении.
        val dateCurrent = Date()
        val dateSMS = Util.stringToDate(dateTime)
        if (dateSMS == null) {
            return ""
        }
        val duration = dateCurrent.time - dateSMS.time
        var diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        if (diffInMinutes < 0) {
            diffInMinutes = 31
        }
        return if (diffInMinutes < 1) {
            context.getString(R.string.now)
        } else if (diffInMinutes > 30) {
            context.getString(R.string.long_ago)
        } else {
            String.format(Locale.US, context.getString(R.string.minutes_ago), diffInMinutes)
        }
    }

}