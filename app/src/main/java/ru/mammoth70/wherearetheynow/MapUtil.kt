package ru.mammoth70.wherearetheynow

import android.content.Context
import android.content.Intent
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Утилиты работы с данными карт.

fun viewLocation(context: Context, record: PointRecord, newTask: Boolean) {
    // Функция получает данные из последней SMS, проверяет их и выводит в выбранную карту.

    if (DataRepository.getUser(record.phone) == null) {
        return
    }
    if (record.latitude !in -90.0..90.0 || record.longitude !in -180.0..180.0) {
        return
    }

    val intent: Intent = when (SettingsManager.selectedMap) {
        MAP_YANDEX -> {
            Intent(context, YandexActivity::class.java)
        }

        MAP_OPENSTREET -> {
            Intent(context, BrowserActivity::class.java)
        }

          else -> Intent(context, TextActivity::class.java)
        }

    intent.apply {
        if (newTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        putExtra(INTENT_EXTRA_SMS_FROM, record.phone)
        putExtra(INTENT_EXTRA_LATITUDE, record.latitude)
        putExtra(INTENT_EXTRA_LONGITUDE, record.longitude)
        putExtra(INTENT_EXTRA_TIME, record.dateTime)
    }

    context.startActivity(intent)
}

fun timePassed(dateTime: String?, context: Context): String {
    // Функция возвращает разницу текстом между текущим временем и временем в пришедшем SMS-сообщении.
    if (dateTime.isNullOrBlank())  return ""
    val dateSMS = stringToDate(dateTime) ?: return ""
    val dateCurrent = Date()
    val durationMs = dateCurrent.time - dateSMS.time
    if (durationMs < 0) return ""

    val calSMS = Calendar.getInstance().apply { time = dateSMS }
    val calNow = Calendar.getInstance().apply { time = dateCurrent }

    fun Calendar.toStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val startOfSMSDay = (calSMS.clone() as Calendar).apply { toStartOfDay() }.timeInMillis
    val startOfNowDay = (calNow.clone() as Calendar).apply { toStartOfDay() }.timeInMillis
    val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val diffInHours = TimeUnit.MILLISECONDS.toHours(durationMs)
    val dayDiff = TimeUnit.MILLISECONDS.toDays(startOfNowDay - startOfSMSDay)

    return when {
       diffInMinutes < 0 -> ""
       diffInMinutes in 0L..3L -> context.getString(R.string.now)
       diffInMinutes in 4L..59L -> String.format(Locale.US, context.getString(R.string.minutes_ago), diffInMinutes)
       diffInHours in 1L..4L && dayDiff == 0L -> String.format(Locale.US, context.getString(R.string.hours_ago), diffInHours)
       dayDiff == 0L -> context.getString(R.string.today)
       dayDiff == 1L -> context.getString(R.string.yesterday)
       dayDiff == 2L -> context.getString(R.string.before_yesterday)
       else -> context.getString(R.string.long_ago)
    }
}