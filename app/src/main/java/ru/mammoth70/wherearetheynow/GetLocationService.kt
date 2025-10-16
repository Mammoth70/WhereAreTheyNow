package ru.mammoth70.wherearetheynow

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.location.LocationServices
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_SMS_TO
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_NEW_VERSION_REQUEST

class GetLocationService : Service() {
    // Класс получает запрос геолокации с телефонным номером запросившего
    // и посылает в ответ SMS-сообщение с данными геолокации.
    // Работает, через вызов объекта GetLocation.

    override fun onCreate() {
        // Функция создаёт сервис, создаёт в нём fusedLocationClient.
        super.onCreate()
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Функция стартует сервис, и получает через Intent телефонный номер SMS-сообщения.
        val smsTo = intent.getStringExtra(INTENT_EXTRA_SMS_TO)
        val request = intent.getBooleanExtra(INTENT_EXTRA_NEW_VERSION_REQUEST,
            false)
        someTask(smsTo, request)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Функцию реализовать мы обязаны. Возврат null.
        return null
    }

    private fun someTask(smsTo: String?, sendRequest: Boolean) {
        // Функция с основной работой сервиса.
        // Запрашивает геолокацию (если есть разрешения), отправляет её в SMS-сообщении,
        // после чего сервис автоматически останавливается.
        // Работает через вызов объекта GetLocation.
        val getLocation = GetLocation()
        getLocation.sendLocation(this, GetLocation.WAY_SMS,
            smsTo, sendRequest)
        stopSelf()
    }

}