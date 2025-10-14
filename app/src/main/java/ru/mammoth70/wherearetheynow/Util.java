package ru.mammoth70.wherearetheynow;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Util {
    // Класс для констант и статических функций

    public static final String INTENT_EXTRA_COLOR = "color";
    public static final String INTENT_EXTRA_SMS_TO = "sms_to";
    public static final String INTENT_EXTRA_NEW_VERSION_REQUEST = "new_version_request";
    public static final String INTENT_EXTRA_SMS_FROM = "sms_from";
    public static final String INTENT_EXTRA_LATITUDE = "latitude";
    public static final String INTENT_EXTRA_LONGITUDE = "longitude";
    public static final String INTENT_EXTRA_TIME = "time";
    public static final String INTENT_EXTRA_MAP = "map";
    public static final String INTENT_EXTRA_MAP_ZOOM = "zoom";
    public static final String INTENT_EXTRA_MAP_TILT = "tilt";
    public static final String INTENT_EXTRA_MAP_CIRCLE = "circle";
    public static final String INTENT_EXTRA_MAP_CIRCLE_RADIUS = "radius";

    public static final String NAME_SETTINGS = "Settings";
    public static final String NAME_LAST_USER = "LastUser";
    public static final String NAME_USE_SERVICE = "UseService";
    public static final String NAME_PHONES = "phones";
    public static final String NAME_MY_PHONE = "myphone";

    public static String myphone = ""; // номер моего телефона

    public static boolean useService = false; // Используем при определении координат сервис или напрямую.

    public static final int MODE_NIGHT_NO = 1;
    public static final int MODE_NIGHT_YES = 2;
    public static final int MODE_NIGHT_FOLLOW_SYSTEM = -1;
    public static final int COLOR_DYNAMIC_NO = -1;
    public static final int COLOR_DYNAMIC_WALLPAPER = 0;
    public static final int COLOR_DYNAMIC_RED = 1;
    public static final int COLOR_DYNAMIC_YELLOW = 2;
    public static final int COLOR_DYNAMIC_GREEN = 3;
    public static final int COLOR_DYNAMIC_BLUE = 4;
    public static final String NAME_THEME_MODE = "theme";
    public static final String NAME_THEME_COLOR = "color";
    public static int themeMode = MODE_NIGHT_FOLLOW_SYSTEM;
    public static int themeColor = COLOR_DYNAMIC_NO;


    public static ArrayList<String> phones = new ArrayList<>(); // список телефонов
    public static ArrayList<String> menuPhones = new ArrayList<>(); // список телефонов, ограниченный
                                                                    // наличием записей геолокации

    public static HashMap<String, String> phone2name = new HashMap<>(); // словарь телефон:контакт
    public static HashMap<Integer, String> id2phone = new HashMap<>(); // словарь id:телефон
    public static HashMap<String, Integer> phone2id = new HashMap<>(); // словарь телефон:id
    public static HashMap<String, String> phone2color = new HashMap<>(); // словарь телефон:цвет

    public static HashMap<String, PointRecord> phone2record = new HashMap<>(); // словарь телефон:point

    public static final String HEADER_REQUEST = "^WATN R$";
    public static final String HEADER_REQUEST_AND_LOCATION = "^WATN R ";
    public static final String HEADER_ANSWER = "^WATN A ";
    public static final String FORMAT_ANSWER = "WATN A lat %1$.6f, lon %2$.6f, time %3$tF %3$tT";
    public static final String FORMAT_REQUEST_AND_LOCATION = "WATN R lat %1$.6f, lon %2$.6f, time %3$tF %3$tT";
    public static final String REGEXP_ANSWER = "^WATN [AR] lat (-?\\d{2,3}\\.\\d{6}), lon (-?\\d{2,3}\\.\\d{6}), time (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$";

    public static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    public static void setThemeMode(int mode) {
        // Метод включает или выключает ночную тему в соответствии с переданными настройками.
        switch (mode) {
            case Util.MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Util.MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static void setAppThemeColor(Application application, int color, boolean refresh) {
        // Метод переключает темы с динамическими цветами в соответствии с переданными настройками.
        switch (color) {
            case (COLOR_DYNAMIC_NO) :
                // Используется для эмуляции отключения динамического цвета во время выполнения.
                // Полностью динамический цвет отключится после перезагрузки приложения.
                if (refresh) {
                    DynamicColors.applyToActivitiesIfAvailable(application,
                            new DynamicColorsOptions.Builder()
                            .setThemeOverlay(R.style.AppTheme_Overlay_Static)
                            .build());
                }
                break;
            case (COLOR_DYNAMIC_WALLPAPER) :
                DynamicColors.applyToActivitiesIfAvailable(application,
                        new DynamicColorsOptions.Builder()
                        .setThemeOverlay(R.style.AppTheme_Overlay_Dynamic)
                        .build());
                break;
            case (COLOR_DYNAMIC_RED) :
                DynamicColors.applyToActivitiesIfAvailable(application,
                        new DynamicColorsOptions.Builder()
                        .setThemeOverlay(R.style.AppTheme_Overlay_Red)
                        .build());
                break;
            case (COLOR_DYNAMIC_YELLOW) :
                DynamicColors.applyToActivitiesIfAvailable(application,
                        new DynamicColorsOptions.Builder()
                        .setThemeOverlay(R.style.AppTheme_Overlay_Yellow)
                        .build());
                break;
            case (COLOR_DYNAMIC_GREEN) :
                DynamicColors.applyToActivitiesIfAvailable(application,
                        new DynamicColorsOptions.Builder()
                        .setThemeOverlay(R.style.AppTheme_Overlay_Green)
                        .build());
                break;
            case (COLOR_DYNAMIC_BLUE) :
                DynamicColors.applyToActivitiesIfAvailable(application,
                        new DynamicColorsOptions.Builder()
                        .setThemeOverlay(R.style.AppTheme_Overlay_Blue)
                        .build());
                break;
        }
    }

    public static Date stringToDate(String dateTime) {
        // Метод преобразовывает строку в дату
        SimpleDateFormat dateFormat =
                new SimpleDateFormat(FORMAT_DATETIME, Locale.getDefault());
        try {
            return dateFormat.parse(dateTime);
        } catch (ParseException ignored) {
            return null;
        }
    }

}