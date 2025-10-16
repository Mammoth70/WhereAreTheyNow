package ru.mammoth70.wherearetheynow

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Util {
    // Объект для констант, синглтонов и статических функций.

    const val INTENT_EXTRA_COLOR: String = "color"
    const val INTENT_EXTRA_SMS_TO: String = "sms_to"
    const val INTENT_EXTRA_NEW_VERSION_REQUEST: String = "new_version_request"
    const val INTENT_EXTRA_SMS_FROM: String = "sms_from"
    const val INTENT_EXTRA_LATITUDE: String = "latitude"
    const val INTENT_EXTRA_LONGITUDE: String = "longitude"
    const val INTENT_EXTRA_TIME: String = "time"
    const val INTENT_EXTRA_MAP: String = "map"
    const val INTENT_EXTRA_MAP_ZOOM: String = "zoom"
    const val INTENT_EXTRA_MAP_TILT: String = "tilt"
    const val INTENT_EXTRA_MAP_CIRCLE: String = "circle"
    const val INTENT_EXTRA_MAP_CIRCLE_RADIUS: String = "radius"

    const val NAME_SETTINGS: String = "Settings"
    const val NAME_USE_SERVICE: String = "UseService"
    const val NAME_MY_PHONE: String = "myphone"

    var myphone: String = "" // номер моего телефона

    var useService: Boolean = false // используем при определении координат сервис или напрямую

    const val MODE_NIGHT_NO: Int = 1
    const val MODE_NIGHT_YES: Int = 2
    const val MODE_NIGHT_FOLLOW_SYSTEM: Int = -1
    const val COLOR_DYNAMIC_NO: Int = -1
    const val COLOR_DYNAMIC_WALLPAPER: Int = 0
    const val COLOR_DYNAMIC_RED: Int = 1
    const val COLOR_DYNAMIC_YELLOW: Int = 2
    const val COLOR_DYNAMIC_GREEN: Int = 3
    const val COLOR_DYNAMIC_BLUE: Int = 4
    const val NAME_THEME_MODE: String = "theme"
    const val NAME_THEME_COLOR: String = "color"
    var themeMode: Int = MODE_NIGHT_FOLLOW_SYSTEM
    var themeColor: Int = COLOR_DYNAMIC_NO

    var phones: ArrayList<String> = ArrayList() // список телефонов
    var menuPhones: ArrayList<String> = ArrayList() // список телефонов, ограниченный наличием записей геолокации
    var phone2name: HashMap<String, String> = HashMap() // словарь телефон:контакт
    var id2phone: HashMap<Int, String> = HashMap() // словарь id:телефон
    var phone2id: HashMap<String, Int> = HashMap() // словарь телефон:id
    var phone2color: HashMap<String, String> = HashMap() // словарь телефон:цвет
    var phone2record: HashMap<String, PointRecord> =  HashMap() // словарь телефон:point
    var lastAnswerRecord: PointRecord? = null // запись с данными последнего ответа

    const val HEADER_REQUEST: String = "^WATN R$"
    const val HEADER_REQUEST_AND_LOCATION: String = "^WATN R "
    const val HEADER_ANSWER: String = "^WATN A "
    const val FORMAT_ANSWER: String = "WATN A lat %1$.6f, lon %2$.6f, time %3\$tF %3\$tT"
    const val FORMAT_REQUEST_AND_LOCATION: String =
        "WATN R lat %1$.6f, lon %2$.6f, time %3\$tF %3\$tT"
    const val REGEXP_ANSWER: String =
        "^WATN [AR] lat (-?\\d{2,3}\\.\\d{6}), lon (-?\\d{2,3}\\.\\d{6}), time (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$"

    const val FORMAT_DATETIME: String = "yyyy-MM-dd HH:mm:ss"

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
        }
    }

    fun stringToDate(dateTime: String): Date? {
        // Функция преобразовывает строку в дату.
        val dateFormat =
            SimpleDateFormat(FORMAT_DATETIME, Locale.getDefault())
        return try {
            dateFormat.parse(dateTime)
        } catch (_: ParseException) {
            null
        }
    }

}