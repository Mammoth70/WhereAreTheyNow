package ru.mammoth70.wherearetheynow

import android.content.Context
import androidx.core.content.edit

object SettingsManager {
    // Объект содержит переменные с настройками.


    const val NAME_SETTINGS = "Settings"

    private const val NAME_COLORS_SPAN_COUNT = "ColorsSpanCount"
    private const val NAME_THEME_MODE = "theme"
    private const val NAME_THEME_COLOR = "color"
    private const val NAME_MAP = "map"
    private const val NAME_MAP_ZOOM = "zoom"
    private const val NAME_MAP_TILT = "tilt"
    private const val NAME_MAP_CIRCLE = "circle"
    private const val NAME_MAP_CIRCLE_RADIUS = "radius"
    private const val NAME_USE_INTERNET = "UseInternet"
    private const val NAME_INTERNET_SERVER = "InternetServer"
    private const val NAME_INTERNET_TOKEN = "InternetToken"

    private val prefs by lazy {
        App.appContext.getSharedPreferences(NAME_SETTINGS, Context.MODE_PRIVATE)
    }


    @Volatile
    private var cachedThemeColor: Int = prefs.getInt(NAME_THEME_COLOR, COLOR_DYNAMIC_NO)
    @Volatile
    private var cachedThemeMode: Int = prefs.getInt(NAME_THEME_MODE, MODE_NIGHT_FOLLOW_SYSTEM)
    @Volatile
    private var cachedColorsSpanCount: Int = prefs.getInt(NAME_COLORS_SPAN_COUNT, 2)
    @Volatile
    private var cachedSelectedMap: Int = prefs.getInt(NAME_MAP, MAP_YANDEX)
    @Volatile
    private var cachedMapZoom: Float = prefs.getFloat(NAME_MAP_ZOOM, MAP_ZOOM_DEFAULT)
    @Volatile
    private var cachedMapTilt: Float = prefs.getFloat(NAME_MAP_TILT, MAP_TILT_DEFAULT)
    @Volatile
    private var cachedMapCircle: Boolean = prefs.getBoolean(NAME_MAP_CIRCLE, MAP_CIRCLE_DEFAULT)
    @Volatile
    private var cachedMapCircleRadius: Float = prefs.getFloat(NAME_MAP_CIRCLE_RADIUS, MAP_CIRCLE_DEFAULT_RADIUS)
    @Volatile
    private var cachedUseInternet: Boolean = prefs.getBoolean(NAME_USE_INTERNET, false)
    @Volatile
    private var cachedInternetServer: String = prefs.getString(NAME_INTERNET_SERVER, "") ?: ""
    @Volatile
    private var cachedInternetToken: String = prefs.getString(NAME_INTERNET_TOKEN, "") ?: ""

    // Тема
    var themeColor: Int   // Цвет темы.
        get() = cachedThemeColor
        set(value) {
            if (value != cachedThemeColor ) {
                cachedThemeColor = value
                prefs.edit { putInt(NAME_THEME_COLOR, value) }
            }
        }

    var themeMode: Int    // Режим темы.
        get() = cachedThemeMode
        set(value)  {
            if (value != cachedThemeMode ) {
                cachedThemeMode = value
                prefs.edit { putInt(NAME_THEME_MODE, value) }
            }
        }



    // UI
    var colorsSpanCount: Int  // Количество колонок в адаптере с цветовыми метками.
        get() = cachedColorsSpanCount
        set(value) {
            if (value != cachedColorsSpanCount ) {
                cachedColorsSpanCount = value
                prefs.edit { putInt(NAME_COLORS_SPAN_COUNT, value) }
            }
        }


    // Настройки карты
    var selectedMap: Int     // Выбор карты для вывода координа.
        get() = cachedSelectedMap
        set(value) {
            if (value != cachedSelectedMap ) {
                cachedSelectedMap = value
                prefs.edit { putInt(NAME_MAP, value) }
            }
        }

    var selectedMapZoom: Float  // Начальный масштаб карты.
        get() = cachedMapZoom
        set(value) {
            if (value != cachedMapZoom ) {
                cachedMapZoom = value
                prefs.edit { putFloat(NAME_MAP_ZOOM, value) }
            }
        }
 
    var selectedMapTilt: Float   // Начальный наклон камеры на Яндекс-карте.
        get() = cachedMapTilt
        set(value) {
            if (value != cachedMapTilt ) {
                cachedMapTilt = value
                prefs.edit { putFloat(NAME_MAP_TILT, value) }
            }
        }

    var selectedMapCircle: Boolean    // Флаг показа круга вокруг метки на Яндекс-карте.
        get() = cachedMapCircle
        set(value) {
            if (value != cachedMapCircle ) {
                cachedMapCircle = value
                prefs.edit { putBoolean(NAME_MAP_CIRCLE, value) }
            }
        }

    var selectedMapCircleRadius: Float   // Начальный радиус круга на Яндекс-карте.
        get() = cachedMapCircleRadius
        set(value) {
            if (value != cachedMapCircleRadius ) {
                cachedMapCircleRadius = value
                prefs.edit { putFloat(NAME_MAP_CIRCLE_RADIUS, value) }
            }
        }


    // Работа через интернет
    var useInternet: Boolean  // Флаг использования интернет-сервера при определении координат сервиса.
        get() = cachedUseInternet
        set(value) {
            if (value != cachedUseInternet ) {
                cachedUseInternet = value
                prefs.edit { putBoolean(NAME_USE_INTERNET, value) }
            }
        }

    var InternetServer: String  // Интернет-сервер.
        get() = cachedInternetServer
        set(value) {
            if (value != cachedInternetServer ) {
                cachedInternetServer = value
                prefs.edit { putString(NAME_INTERNET_SERVER, value) }
            }
        }

    var InternetToken: String  // Токен авторизации на интернет-сервере.
        get() = cachedInternetToken
        set(value) {
            if (value != cachedInternetToken ) {
                cachedInternetToken = value
                prefs.edit { putString(NAME_INTERNET_TOKEN, value) }
            }
        }

}