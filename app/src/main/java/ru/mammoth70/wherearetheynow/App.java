package ru.mammoth70.wherearetheynow;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.android.material.color.DynamicColors;
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
        SharedPreferences settings = getSharedPreferences(Util.NAME_SETTINGS, MODE_PRIVATE);

        // Считываем из SharedPreferences, как определять цвет темы.
        Util.themeColor = settings.getInt(Util.nameThemeColor, Util.themeColor);
        // Включение динамического цвета.
        if (Util.themeColor == Util.COLOR_DYNAMIC_YES) {
            DynamicColors.applyToActivitiesIfAvailable(this);
        }

        // Считываем из SharedPreferences, как определять режим темы.
        Util.themeMode = settings.getInt(Util.nameThemeMode, Util.themeMode);
        // Включение ночного режима.
        Util.setThemeMode(Util.themeMode);

        // Считываем из SharedPreferences, как определять геолокацию - через сервис или напрямую.
        Util.useService = settings.getBoolean(Util.NAME_USE_SERVICE, Util.useService);

        // Считываем из SharedPreferences вид и настройки выбранной карты для вывода геолокации.
        MapUtil.selectedMap = settings.getInt(MapUtil.NAME_MAP, MapUtil.MAP_DEFAULT);
        MapUtil.selectedMapZoom = settings.getFloat(MapUtil.NAME_MAP_ZOOM, MapUtil.MAP_ZOOM_DEFAULT);
        MapUtil.selectedMapTilt = settings.getFloat(MapUtil.NAME_MAP_TILT, MapUtil.MAP_TILT_DEFAULT);
        MapUtil.selectedMapCircle = settings.getBoolean(MapUtil.NAME_MAP_CIRCLE, MapUtil.MAP_CIRCLE_DEFAULT);
        MapUtil.selectedMapCircleRadius = settings.getFloat(MapUtil.NAME_MAP_CIRCLE_RADIUS, MapUtil.MAP_CIRCLE_DEFAULT_RADIUS);

        // Считываем из SharedPreferences номер собственного телефона.
        // Он нужен, чтобы не отправлять самому себе SMS-сообщения, а получать геолокацию напрямую.
        Util.myphone = settings.getString(Util.NAME_MY_PHONE, Util.myphone);

        // Считываем из SharedPreferences множество разрешенных к работе телефонов.
        // Они должны были готовы к началу работы BroadcastReceiver, не загружаясь из БД.
        Set<String> phonesSet = settings.getStringSet(Util.NAME_PHONES, new HashSet<>());
        Util.phones = new ArrayList<>(phonesSet);
    }

}