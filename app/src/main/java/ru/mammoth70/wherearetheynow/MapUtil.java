package ru.mammoth70.wherearetheynow;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MapUtil {
    // Класс содержит настройки карт и утилиты работы с данными карт.
    public static final int MAP_TEXT = 0;
    public static final int MAP_YANDEX = 1;
    public static final int MAP_OPENSTREET = 2;
    public static final int MAP_DEFAULT = MAP_YANDEX;
    public static final String NAME_MAP = "map";
    public static final String NAME_MAP_ZOOM = "zoom";
    public static final String NAME_MAP_TILT = "tilt";
    public static final String NAME_MAP_CIRCLE = "circle";
    public static final String NAME_MAP_CIRCLE_RADIUS = "radius";

    public static final float MAP_ZOOM_DEFAULT = 17f;
    public static final float MAP_TILT_DEFAULT = 30f;
    public static final boolean MAP_CIRCLE_DEFAULT = true;
    public static final float MAP_CIRCLE_DEFAULT_RADIUS = 70f;
    private static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    public static int selectedMap = MAP_DEFAULT;
    public static float selectedMapZoom = MAP_ZOOM_DEFAULT;
    public static float selectedMapTilt = MAP_TILT_DEFAULT;
    public static boolean selectedMapCircle = MAP_CIRCLE_DEFAULT;
    public static float selectedMapCircleRadius = MAP_CIRCLE_DEFAULT_RADIUS;

    static public void viewLocation(Context context, PointRecord record, boolean new_task) {
        // Метод получает данные из последней SMS,
        // проверяет их и выводит в выбранную карту.
        if ((record.latitude > -90) && (record.latitude < 90) &&
                (record.longitude > -180) && (record.longitude < 180) &&
                (Util.phones.contains(record.phone))) {
            Intent intent;
            switch (selectedMap) {
                case MAP_YANDEX:
                    intent = new Intent(context, YandexActivity.class);
                    intent.putExtra(Util.INTENT_EXTRA_MAP_ZOOM, selectedMapZoom);
                    intent.putExtra(Util.INTENT_EXTRA_MAP_TILT, selectedMapTilt);
                    intent.putExtra(Util.INTENT_EXTRA_MAP_CIRCLE, selectedMapCircle);
                    intent.putExtra(Util.INTENT_EXTRA_MAP_CIRCLE_RADIUS, selectedMapCircleRadius);
                    break;
                case MAP_OPENSTREET:
                    intent = new Intent(context, BrowserActivity.class);
                    intent.putExtra(Util.INTENT_EXTRA_MAP, selectedMap);
                    intent.putExtra(Util.INTENT_EXTRA_MAP_ZOOM, selectedMapZoom);
                    break;
                default:
                    intent = new Intent(context, TextActivity.class);
                    break;
            }
            if (new_task) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.putExtra(Util.INTENT_EXTRA_SMS_FROM, record.phone);
            intent.putExtra(Util.INTENT_EXTRA_LATITUDE, record.latitude);
            intent.putExtra(Util.INTENT_EXTRA_LONGITUDE, record.longitude);
            intent.putExtra(Util.INTENT_EXTRA_TIME, record.datetime);
            context.startActivity(intent);
        }
    }

    static public void setLastAnswer(Context context, PointRecord record) {
        // Метод сохраняет в HashMap, SharedPreferences и в БД данные с последнего ответа на запрос.
        if ((record.latitude > -90) && (record.latitude < 90) &&
                (record.longitude > -180) && (record.longitude < 180) &&
                (Util.phones.contains(record.phone))) {
            Util.phone2record.put(record.phone, record);
            SharedPreferences settings = context.getSharedPreferences(Util.NAME_LAST_USER, MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = settings.edit();
            prefEditor.putString(Util.INTENT_EXTRA_SMS_FROM, record.phone);
            prefEditor.putString(Util.INTENT_EXTRA_LATITUDE,
                    String.format(Locale.US, PointRecord.FORMAT_DOUBLE, record.latitude));
            prefEditor.putString(Util.INTENT_EXTRA_LONGITUDE,
                    String.format(Locale.US, PointRecord.FORMAT_DOUBLE, record.longitude));
            prefEditor.putString(Util.INTENT_EXTRA_TIME, record.datetime);
            prefEditor.apply();
            try (DBhelper dBhelper = new DBhelper(context)) {
                dBhelper.setLastPoint(record);
            }
        }
    }

    static public PointRecord getLastAnswer(Context context) {
        // Метод считывает из SharedPreferences данные с последнего ответа на запрос.
        SharedPreferences settings = context.getSharedPreferences(Util.NAME_LAST_USER, MODE_PRIVATE);
        return new PointRecord(
        settings.getString(
                Util.INTENT_EXTRA_SMS_FROM, ""),
                Double.parseDouble(settings.getString(Util.INTENT_EXTRA_LATITUDE, "0")),
                Double.parseDouble(settings.getString(Util.INTENT_EXTRA_LONGITUDE, "0")),
                settings.getString(Util.INTENT_EXTRA_TIME, ""));
    }

    static public String timePassed(String datetime, Context context) {
        // Метод возвращает разницу в минутах между текущим временем и временем в пришедшем SMS-сообщении.
        // И выводит в минутах.
        Date dateSMS;
        Date dateCurrent = new Date();
        SimpleDateFormat dateFormat =
                new SimpleDateFormat(FORMAT_DATETIME, Locale.getDefault());
        try {
            dateSMS = dateFormat.parse(datetime);
        } catch (ParseException e) {
            return "";
        }
        long diffInMinutes = 31;
        if (dateSMS != null) {
            long duration = dateCurrent.getTime() - dateSMS.getTime();
            diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            if (diffInMinutes < 0) {
                diffInMinutes = 31;
            }
        }
        if (diffInMinutes < 1) {
            return context.getString(R.string.now);
        } else if (diffInMinutes > 30) {
            return context.getString(R.string.long_ago);
        } else {
            return String.format(Locale.US,context.getString(R.string.minutes_ago), diffInMinutes);
        }
    }

}