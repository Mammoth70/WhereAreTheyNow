package ru.mammoth70.wherearetheynow

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.android.gms.location.LocationServices

class GetLocationService : Service() {
    // Класс получает запрос геолокации с телефонным номером запросившего
    // и посылает в ответ SMS-сообщение с данными геолокации.
    // Работает, через вызов объекта GetLocation.

    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        // Функция создаёт сервис, создаёт в нём fusedLocationClient.

        super.onCreate()
        createNotificationChannel()
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Функция стартует сервис, и получает через Intent телефонный номер SMS-сообщения.

        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val smsTo = intent.getStringExtra(INTENT_EXTRA_SMS_TO)
        val request = intent.getBooleanExtra(INTENT_EXTRA_NEW_VERSION_REQUEST, false)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_location_on)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val fineLoc = androidx.core.content.ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (fineLoc == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startForeground(NOTIFICATION_ID, notification)
            someTask(smsTo, request)
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun someTask(smsTo: String?, sendRequest: Boolean) {
        // Функция с основной работой сервиса.
        // Запрашивает геолокацию (если есть разрешения), отправляет её в SMS-сообщении,
        // после чего сервис автоматически останавливается.
        // Работает через вызов объекта GetLocation.

        val getLocation = GetLocation()
        getLocation.sendLocation(this, GetLocation.WAY_SMS, smsTo, sendRequest){ stopSelf() }
    }

    private fun createNotificationChannel() {
        // Функция открывает NotificationChannel и информарует о работе сервиса.

        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

}