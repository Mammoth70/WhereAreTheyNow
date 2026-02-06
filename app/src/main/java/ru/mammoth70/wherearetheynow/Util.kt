package ru.mammoth70.wherearetheynow

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Константы и статические функции.

const val INTENT_EXTRA_COLOR = "color"
const val INTENT_EXTRA_SMS_TO = "sms_to"
const val INTENT_EXTRA_NEW_VERSION_REQUEST = "new_version_request"

const val INTENT_EXTRA_SMS_FROM = "sms_from"
const val INTENT_EXTRA_LATITUDE = "latitude"
const val INTENT_EXTRA_LONGITUDE = "longitude"
const val INTENT_EXTRA_TIME = "time"

const val HEADER_REQUEST = "^WATN R$"
const val HEADER_REQUEST_AND_LOCATION = "^WATN R "
const val HEADER_ANSWER = "^WATN A "
const val FORMAT_ANSWER = $$"WATN A lat %1$.6f, lon %2$.6f, time %3$tF %3$tT"
const val FORMAT_REQUEST_AND_LOCATION = $$"WATN R lat %1$.6f, lon %2$.6f, time %3$tF %3$tT"
const val REGEXP_ANSWER =
    "^WATN [AR] lat (-?\\d{2,3}\\.\\d{6}), lon (-?\\d{2,3}\\.\\d{6}), time (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$"

const val FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss"

fun themeMode(mode: Int) {
    // Функция включает или выключает ночную тему в соответствии с переданными настройками.

    when (mode) {
        MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}

fun setAppThemeColor(application: Application, color: Int, refresh: Boolean) {
    // Функция переключает темы с динамическими цветами в соответствии с переданными настройками.

    when (color) {
        COLOR_DYNAMIC_NO ->
            // Эмуляция отключения динамического цвета во время выполнения.
            // Полностью динамический цвет отключится только после перезагрузки приложения.
            if (refresh) {
                DynamicColors.applyToActivitiesIfAvailable(
                    application,
                    DynamicColorsOptions.Builder()
                        .setThemeOverlay(R.style.AppTheme_Overlay_Static)
                        .build()
                )
            }

        COLOR_DYNAMIC_WALLPAPER -> DynamicColors.applyToActivitiesIfAvailable(
            application,
            DynamicColorsOptions.Builder()
                .setThemeOverlay(R.style.AppTheme_Overlay_Dynamic)
                .build()
        )

        COLOR_DYNAMIC_RED -> DynamicColors.applyToActivitiesIfAvailable(
            application,
            DynamicColorsOptions.Builder()
                .setThemeOverlay(R.style.AppTheme_Overlay_Red)
                .build()
        )

        COLOR_DYNAMIC_YELLOW -> DynamicColors.applyToActivitiesIfAvailable(
            application,
            DynamicColorsOptions.Builder()
                .setThemeOverlay(R.style.AppTheme_Overlay_Yellow)
                .build()
        )

        COLOR_DYNAMIC_GREEN -> DynamicColors.applyToActivitiesIfAvailable(
            application,
            DynamicColorsOptions.Builder()
                .setThemeOverlay(R.style.AppTheme_Overlay_Green)
                .build()
        )

        COLOR_DYNAMIC_BLUE -> DynamicColors.applyToActivitiesIfAvailable(
            application,
            DynamicColorsOptions.Builder()
                .setThemeOverlay(R.style.AppTheme_Overlay_Blue)
                .build()
        )

        COLOR_DYNAMIC_M3 -> DynamicColors.applyToActivitiesIfAvailable(
            application,
            DynamicColorsOptions.Builder()
                .setThemeOverlay(R.style.AppTheme_Overlay_M3)
                .build()
        )
    }
}

fun stringToDate(dateTime: String): Date? {
    // Функция преобразовывает строку в дату.

    val dateFormat =
        SimpleDateFormat(FORMAT_DATETIME, Locale.US)
    return try {
        dateFormat.parse(dateTime)
    } catch (e: ParseException) {
        LogSmart.e("Util", "ParseException в stringToDate(${dateTime})", e)
        null
    }
}

object LogSmart {
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        @Suppress("KotlinConstantConditions")
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
    }

    @Suppress("unused")
    fun d(tag: String, message: String) {
        @Suppress("KotlinConstantConditions")
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
}