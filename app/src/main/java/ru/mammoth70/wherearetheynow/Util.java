package ru.mammoth70.wherearetheynow;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.HashMap;

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

    public static final String nameSettings = "Settings";
    public static final String nameLastUser = "LastUser";
    public static final String nameUseService = "UseService";
    public static final String namePhones = "phones";
    public static final String nameMyPhone = "myphone";

    public static String myphone = ""; // номер моего телефона

    public static boolean useService = false; // Используем при определении координат сервис или напрямую.

    public static final int MODE_NIGHT_NO = 1;
    public static final int MODE_NIGHT_YES = 2;
    public static final int MODE_NIGHT_FOLLOW_SYSTEM = -1;
    public static final int COLOR_DYNAMIC_NO = 1;
    public static final int COLOR_DYNAMIC_YES = 2;
    public static final String nameThemeMode = "theme";
    public static final String nameThemeColor = "color";
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
}