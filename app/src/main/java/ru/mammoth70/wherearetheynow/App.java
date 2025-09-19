package ru.mammoth70.wherearetheynow;

import android.app.Application;
import android.content.SharedPreferences;

import com.yandex.mapkit.MapKitFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class App extends Application {
    // Класс приложения.
    // Приложение предназначено для определения местоположения родтвенников и друзей.
    // Обмен запросами положения и ответами с геолокацией реализван через SMS-сообщения.

    @Override
    public void onCreate() {
        // Здесь делаются самые стартовые настройки.
        super.onCreate();

        // Считываем из настроек Gradle Yandex MAPKIT-API key.
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY);

        // Инициализируем чтение из SharedPreferences.
        SharedPreferences settings = getSharedPreferences(Util.nameSettings, MODE_PRIVATE);

        // Считываем из SharedPreferences, как определять тему.
        Util.modeNight = settings.getInt(Util.nameThemeMode, Util.modeNight);
        // Включение ночной темы.
        Util.setNightTheme(Util.modeNight);

        // Считываем из SharedPreferences, как определять геолокацию - через сервис или напрямую.
        Util.useService = settings.getBoolean(Util.nameUseService, Util.useService);

        // Считываем из SharedPreferences вид и настройки выбранной карты для вывода геолокации.
        MapUtil.selectedMap = settings.getInt(MapUtil.nameMap, MapUtil.MAP_DEFAULT);
        MapUtil.selectedMapZoom = settings.getFloat(MapUtil.nameMapZoom, MapUtil.MAP_ZOOM_DEFAULT);
        MapUtil.selectedMapTilt = settings.getFloat(MapUtil.nameMapTilt, MapUtil.MAP_TILT_DEFAULT);
        MapUtil.selectedMapCircle = settings.getBoolean(MapUtil.nameMapCircle, MapUtil.MAP_CIRCLE_DEFAULT);

        // Считываем из SharedPreferences номер собственного телефона.
        // Он нужен, чтобы не отправлять самому себе SMS-сообщения, а получать геолокацию напрямую.
        Util.myphone = settings.getString(Util.nameMyPhone, Util.myphone);

        // Считываем из SharedPreferences множество разрешенных к работе телефонов.
        // Они должны были готовы к началу работы BroadcastReceiver, не загружаясь из БД.
        Set<String> phonesSet = settings.getStringSet(Util.namePhones, new HashSet<>());
        Util.phones = new ArrayList<>(phonesSet);
    }
}