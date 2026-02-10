package ru.mammoth70.wherearetheynow

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
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
    return calculateTimePassed(
        dateTime = dateTime,
        now = Date(),
        getString = { resId, arg ->
            if (arg != null) {
                String.format(Locale.US, context.getString(resId), arg)
            } else {
                context.getString(resId)
            }
        }
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun calculateTimePassed(
    dateTime: String?,
    now: Date,
    getString: (resId: Int, formatArg: Any?) -> String
): String {
    // Функция с логикой для timePassed.

    if (dateTime.isNullOrBlank()) return ""

    val dateSMS = stringToDate(dateTime) ?: return ""
    val durationMs = now.time - dateSMS.time

    val calSMS = Calendar.getInstance().apply { time = dateSMS }
    val calNow = Calendar.getInstance().apply { time = now }

    fun Calendar.toStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val startOfSMSDay = (calSMS.clone() as Calendar).apply { toStartOfDay() }.timeInMillis
    val startOfNowDay = (calNow.clone() as Calendar).apply { toStartOfDay() }.timeInMillis

    val diffInSeconds = durationMs / 1000

    val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val diffInHours = TimeUnit.MILLISECONDS.toHours(durationMs)
    val dayDiff = TimeUnit.MILLISECONDS.toDays(startOfNowDay - startOfSMSDay)

    return when {
        diffInSeconds < -120L -> ""
        diffInSeconds in -120L..239L -> getString(R.string.now, null)
        diffInSeconds in 240L..3599L -> getString(R.string.minutes_ago, diffInMinutes)
        diffInSeconds in 3600L..14400L && dayDiff == 0L -> getString(R.string.hours_ago, diffInHours)
        dayDiff == 0L -> getString(R.string.today, null)
        dayDiff == 1L -> getString(R.string.yesterday, null)
        dayDiff == 2L -> getString(R.string.before_yesterday, null)
        else -> getString(R.string.long_ago, null)
    }
}