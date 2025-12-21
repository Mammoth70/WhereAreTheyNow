package ru.mammoth70.wherearetheynow

import android.content.Context
import android.content.Intent
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// Настройки карт и утилиты работы с данными карт.

const val MAP_TEXT = 0
const val MAP_YANDEX = 1
const val MAP_OPENSTREET = 2
const val MAP_DEFAULT = MAP_YANDEX
const val NAME_MAP = "map"
const val NAME_MAP_ZOOM = "zoom"
const val NAME_MAP_TILT = "tilt"
const val NAME_MAP_CIRCLE = "circle"
const val NAME_MAP_CIRCLE_RADIUS = "radius"

const val MAP_ZOOM_DEFAULT = 17f
const val MAP_TILT_DEFAULT = 30f
const val MAP_CIRCLE_DEFAULT = true
const val MAP_CIRCLE_DEFAULT_RADIUS = 70f

var selectedMap = MAP_DEFAULT
var selectedMapZoom = MAP_ZOOM_DEFAULT
var selectedMapTilt = MAP_TILT_DEFAULT
var selectedMapCircle = MAP_CIRCLE_DEFAULT
var selectedMapCircleRadius = MAP_CIRCLE_DEFAULT_RADIUS

fun viewLocation(context: Context, record: PointRecord, newTask: Boolean) {
    // Функция получает данные из последней SMS,
    // проверяет их и выводит в выбранную карту.
    if (record.phone !in phones) {
        return
    }
    if ((record.latitude < -90) || (record.latitude > 90) ||
        (record.longitude < -180) || (record.longitude > 180)) {
        return
    }
    val intent: Intent = when (selectedMap) {
        MAP_YANDEX -> {
            Intent(context, YandexActivity::class.java)
        }

        MAP_OPENSTREET -> {
            Intent(context, BrowserActivity::class.java)
        }

          else -> Intent(context, TextActivity::class.java)
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

fun timePassed(dateTime: String?, context: Context): String {
    // Функция возвращает разницу текстом между текущим временем
    // и временем в пришедшем SMS-сообщении.
    if (dateTime.isNullOrBlank()) {
        return ""
    }
    val dateSMS = stringToDate(dateTime) ?: return ""
    val dateCurrent = Date()
    val duration = dateCurrent.time - dateSMS.time
    val calendar: Calendar = Calendar.getInstance()
    calendar.setTime(dateSMS)
    val daySMS = calendar.get(Calendar.DAY_OF_WEEK)
    calendar.setTime(dateCurrent)
    val dayCurrent = calendar.get(Calendar.DAY_OF_WEEK)
    val dayDuration = abs(dayCurrent - daySMS)
    val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration)
    val diffInHours = TimeUnit.MILLISECONDS.toHours(duration)
    return when {
       diffInMinutes < 0 -> ""
       diffInMinutes in 0L..3L -> context.getString(R.string.now)
       diffInMinutes in 4L..59L -> String.format(Locale.US, context.getString(R.string.minutes_ago), diffInMinutes)
       diffInHours in 0L..4L -> String.format(Locale.US, context.getString(R.string.hours_ago), diffInHours)
       diffInHours in 5L..72L && dayDuration == 0 -> context.getString(R.string.today)
       diffInHours in 5L..72L && dayDuration == 1 -> context.getString(R.string.yesterday)
       diffInHours in 5L..72L && dayDuration == 2 -> context.getString(R.string.before_yesterday)
       else -> context.getString(R.string.long_ago)
    }
}