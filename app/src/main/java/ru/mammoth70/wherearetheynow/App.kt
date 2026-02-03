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
            private set
    }

    override fun onCreate() {
        // Функция вызывается при запуске приложения.
        // Производит выполнение стартовых настроек.

        super.onCreate()
        appContext = applicationContext

        // Чтение из настроек MAPKIT-API key и его установка.
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)

        // Чтение из БД списка разрешенных телефонов и словарей контактов.
        DataRepository.refreshData()

        // Установка режима и цвета темы приложения.
        themeMode(SettingsManager.themeMode)
        setAppThemeColor(this, SettingsManager.themeColor, false)
    }

}