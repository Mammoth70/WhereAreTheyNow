package ru.mammoth70.wherearetheynow

import android.app.Application
import android.content.Context
import com.yandex.mapkit.MapKitFactory

class App : Application() {
    // Класс приложения.
    // Приложение предназначено для определения местоположения родственников и друзей.
    // Обмен запросами положения и ответами с геолокацией реализован через SMS-сообщения.

    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        // Функция вызывается при запуске приложения.
        // Производит выполнение стартовых настроек.
        super.onCreate()
        appContext = applicationContext

        // Чтение из настроек MAPKIT-API key и его установка.
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)

        // Инициализация чтения из SharedPreferences.
        val settings = getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)

        // Чтение из SharedPreferences, как определять цвет темы.
        themeColor = settings.getInt(NAME_THEME_COLOR, themeColor)
        // Определение цвета темы.
        setAppThemeColor(this, themeColor, false)

        // Чтение из SharedPreferences, как определять режим темы.
        themeMode = settings.getInt(NAME_THEME_MODE, themeMode)
        // Определение режима темы.
        themeMode(themeMode)

        // Чтение из SharedPreferences, как определять геолокацию - через сервис или напрямую.
        useService = settings.getBoolean(NAME_USE_SERVICE, useService)

        // Чтение из SharedPreferences, количество колонок в ColorsActivity.
        colorsSpanCount = settings.getInt(NAME_COLORS_SPAN_COUNT, COLORS_SPAN_COUNT_DEFAULT)

        // Чтение из SharedPreferences вида и настройки выбранной карты для вывода геолокации.
        selectedMap = settings.getInt(NAME_MAP, MAP_DEFAULT)
        selectedMapZoom = settings.getFloat(NAME_MAP_ZOOM, MAP_ZOOM_DEFAULT)
        selectedMapTilt = settings.getFloat(NAME_MAP_TILT, MAP_TILT_DEFAULT)
        selectedMapCircle = settings.getBoolean(NAME_MAP_CIRCLE,
            MAP_CIRCLE_DEFAULT)
        selectedMapCircleRadius = settings.getFloat(NAME_MAP_CIRCLE_RADIUS,
            MAP_CIRCLE_DEFAULT_RADIUS)

        // Чтение из SharedPreferences номера собственного телефона.
        // Он нужен, чтобы не отправлять самому себе SMS-сообщения, а получать геолокацию напрямую.
        myphone = settings.getString(NAME_MY_PHONE, myphone)?: ""

        // Чтение из БД списка разрешенных телефонов и словарей контактов.
        DBhelper.dbHelper.readUsers()

        // Очистка "висящих" записей.
        DBhelper.dbHelper.checkRecords()

        // Чтение из БД данных с последнего ответа на запрос.
        lastAnswerRecord = DBhelper.dbHelper.readLastAnswer()
    }

}