package ru.mammoth70.wherearetheynow

import android.app.Application
import android.content.Context
import com.yandex.mapkit.MapKitFactory
import ru.mammoth70.wherearetheynow.Util.NAME_SETTINGS
import ru.mammoth70.wherearetheynow.Util.NAME_THEME_COLOR
import ru.mammoth70.wherearetheynow.Util.NAME_THEME_MODE
import ru.mammoth70.wherearetheynow.Util.NAME_USE_SERVICE
import ru.mammoth70.wherearetheynow.Util.NAME_MY_PHONE
import ru.mammoth70.wherearetheynow.MapUtil.NAME_MAP
import ru.mammoth70.wherearetheynow.MapUtil.MAP_DEFAULT
import ru.mammoth70.wherearetheynow.MapUtil.NAME_MAP_ZOOM
import ru.mammoth70.wherearetheynow.MapUtil.MAP_ZOOM_DEFAULT
import ru.mammoth70.wherearetheynow.MapUtil.NAME_MAP_TILT
import ru.mammoth70.wherearetheynow.MapUtil.MAP_TILT_DEFAULT
import ru.mammoth70.wherearetheynow.MapUtil.NAME_MAP_CIRCLE
import ru.mammoth70.wherearetheynow.MapUtil.MAP_CIRCLE_DEFAULT
import ru.mammoth70.wherearetheynow.MapUtil.NAME_MAP_CIRCLE_RADIUS
import ru.mammoth70.wherearetheynow.MapUtil.MAP_CIRCLE_DEFAULT_RADIUS

class App : Application() {
    // Класс приложения.
    // Приложение предназначено для определения местоположения родтвенников и друзей.
    // Обмен запросами положения и ответами с геолокацией реализован через SMS-сообщения.

    companion object {
        lateinit var application: Application
        lateinit var appContext: Context
    }

    override fun onCreate() {
        // Функция вызывается при запуске приложения.
        // Производит выполнение стартовых настроек.
        super.onCreate()
        application = this
        appContext = applicationContext

        // Чтение из настроек MAPKIT-API key и его установка.
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)

        // Инициализация чтения из SharedPreferences.
        val settings = getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)

        // Чтение из SharedPreferences, как определять цвет темы.
        Util.themeColor = settings.getInt(NAME_THEME_COLOR, Util.themeColor)
        // Определение цвета темы.
        Util.setAppThemeColor(application, Util.themeColor, false)

        // Чтение из SharedPreferences, как определять режим темы.
        Util.themeMode = settings.getInt(NAME_THEME_MODE, Util.themeMode)
        // Определение режима темы.
        Util.themeMode(Util.themeMode)

        // Чтение из SharedPreferences, как определять геолокацию - через сервис или напрямую.
        Util.useService = settings.getBoolean(NAME_USE_SERVICE, Util.useService)

        // Чтение из SharedPreferences вида и настройки выбранной карты для вывода геолокации.
        MapUtil.selectedMap = settings.getInt(NAME_MAP, MAP_DEFAULT)
        MapUtil.selectedMapZoom = settings.getFloat(NAME_MAP_ZOOM, MAP_ZOOM_DEFAULT)
        MapUtil.selectedMapTilt = settings.getFloat(NAME_MAP_TILT, MAP_TILT_DEFAULT)
        MapUtil.selectedMapCircle = settings.getBoolean(NAME_MAP_CIRCLE,
            MAP_CIRCLE_DEFAULT)
        MapUtil.selectedMapCircleRadius = settings.getFloat(NAME_MAP_CIRCLE_RADIUS,
            MAP_CIRCLE_DEFAULT_RADIUS)

        // Чтение из SharedPreferences номера собственного телефона.
        // Он нужен, чтобы не отправлять самому себе SMS-сообщения, а получать геолокацию напрямую.
        Util.myphone = settings.getString(NAME_MY_PHONE, Util.myphone)?: ""

        // Чтение из БД списка разрешенных телефонов и словарей контактов.
        DBhelper.dbHelper.readUsers()

        // Очистка "висящих" записей.
        DBhelper.dbHelper.checkRecords()

        // Чтение из SharedPreferences данных с последнего ответа на запрос.
        Util.lastAnswerRecord = DBhelper.dbHelper.readLastAnswer()
    }

}