package ru.mammoth70.wherearetheynow;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class GetLocationService extends Service {
    // Класс получает запрос геолокации с телефонным номером запросившего
    // и посылает в ответ SMS-сообщение с данными геолокации.
    // Работает, через вызов объекта GetLocation.
    FusedLocationProviderClient fusedLocationClient;

    public void onCreate() {
        // Метод создаёт сервис, создаёт в нём fusedLocationClient.
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        // Метод стартует сервис, и получает через Intent телефонный номер SMS-сообщения.
        String smsTo = intent.getStringExtra(Util.INTENT_EXTRA_SMS_TO);
        Boolean request = intent.getBooleanExtra(Util.INTENT_EXTRA_NEW_VERSION_REQUEST, false);
        someTask(smsTo, request);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Метод реализовать мы обязаны, возвращаем null.
        return null;
    }

    private void someTask(String smsTo, Boolean sendRequest) {
        // Метод с основной работой сервиса.
        // Запрашивает геолокацию (если есть разрешения), отправляет её в SMS-сообщении,
        // после чего сервис автоматически останавливается.
        // Работает через вызов объекта GetLocation.
        GetLocation getLocation = new GetLocation();
        getLocation.sendLocation(this, GetLocation.WAY_SMS, smsTo, sendRequest);
        stopSelf();
    }

}