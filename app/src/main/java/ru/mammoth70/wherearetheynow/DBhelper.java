package ru.mammoth70.wherearetheynow;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

public class DBhelper extends SQLiteOpenHelper {
    // Класс обслуживает базу данных со списком пользователей и прочими структурами пользователей.
    private static final int DB_VERSION = 3; // версия БД

    public DBhelper(Context context) {
        super(context, "watnDB", null, DB_VERSION);
    }

    private final String createTableUsersSting = "CREATE TABLE IF NOT EXISTS users " +
            "(id integer PRIMARY KEY AUTOINCREMENT, phone text UNIQUE, name text, color text);";
    private final String createTablePointsSting = "CREATE TABLE IF NOT EXISTS points " +
            "(phone text UNIQUE, latitude text, longitude text, datetime text);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Метод создаёт таблицы users и points.
        db.execSQL(createTableUsersSting);
        db.execSQL(createTablePointsSting);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Метод делает апгрейд БД.
        if (oldVersion == 1 && newVersion > 1) {
            // Удаляем из таблицы users поле label.
            // Снимаем ограничение на уникальность поля name в таблице users.
            // Добавляем таблицу points.
            db.beginTransaction();
            try {
                db.execSQL("CREATE TEMPORARY TABLE users_temp " +
                        "(id integer, phone text, name text, color text);");
                db.execSQL("INSERT INTO users_temp SELECT id, phone, name, color FROM users;");
                db.execSQL("DROP TABLE users;");
                db.execSQL(createTableUsersSting);
                db.execSQL("INSERT INTO users SELECT id, phone, name, color FROM users_temp;");
                db.execSQL("DROP TABLE users_temp;");
                db.execSQL(createTablePointsSting);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
        if (oldVersion == 2 && newVersion > 2) {
            // Добавляем таблицу points.
            db.beginTransaction();
            try {
                db.execSQL(createTablePointsSting);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public void getUsers() {
        // Метод считывает список разрешенных телефонов и словари пользователей из БД.
        SQLiteDatabase db = getReadableDatabase();
        String execSting = "SELECT * FROM users;";
                Cursor cursor = db.rawQuery(execSting, null);
        int id;
        String phone;
        String name;
        String color;
        PointRecord record;

        Util.phones.clear();
        Util.id2phone.clear();
        Util.phone2id.clear();
        Util.phone2name.clear();
        Util.phone2color.clear();
        Util.phone2record.clear();
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            color = cursor.getString(cursor.getColumnIndexOrThrow("color"));
            Util.phones.add(phone);
            Util.id2phone.put(id, phone);
            Util.phone2id.put(phone, id);
            Util.phone2name.put(phone, name);
            Util.phone2color.put(phone, color);
            record = getLastPoint(phone);
            if (record!= null) {
                Util.phone2record.put(phone, record);
            }
        }
        cursor.close();
        db.close();
    }

    public void getMenuUsers() {
        // Метод считывает список телефонов, отортированный по id ASC,
        // имеющих координаты, отсортированные по дате DESC, и по id ASC,
        // и ограниченные заданным (10-ю) количеством записей.
        // Используется для построения меню.
        String phone;
        Util.menuPhones.clear();
        SQLiteDatabase db = getReadableDatabase();
        String execSting =
                "SELECT users.phone AS phone1 FROM " +
                "(SELECT points.phone AS phone2 " +
                "FROM points INNER JOIN users ON points.phone = users.phone " +
                "ORDER BY points.datetime DESC, users.id ASC LIMIT 10 OFFSET 0) AS tmp " +
                "INNER JOIN users ON tmp.phone2 = users.phone ORDER BY users.id;";
        Cursor cursor = db.rawQuery(execSting, null);
        while (cursor.moveToNext()) {
            phone = cursor.getString(cursor.getColumnIndexOrThrow("phone1"));
            Util.menuPhones.add(phone);
        }
        cursor.close();
        db.close();
    }

    public boolean addUser(String phone, String name, String color) {
        // Метод добавляет запись пользователя в БД и обновляет структуры.
        // Возвращает true, если успешно и false, если нет.
        if (!Util.phones.contains(phone)) {
            SQLiteDatabase db = getReadableDatabase();
            String execSting = "INSERT OR IGNORE INTO users (phone, name, color) VALUES " +
                    "('" + phone + "', '" + name + "', '" + color + "');";
            try {
                db.execSQL(execSting);
            } catch (SQLException ignored) {
                db.close();
                return false;
            }
            db.close();
            getUsers();
            return (Util.phones.contains(phone));
        } else {
            return false;
        }
    }

    public boolean editUser(int id, String phone, String name, String color) {
        // Метод изменяет запись пользователя в БД и обновляет структуры.
        // Возвращает true, если успешно и false, если нет.
        if (Util.id2phone.containsKey(id)) {
            SQLiteDatabase db = getReadableDatabase();
            String execSting = "UPDATE users SET " +
                    "phone = '" + phone + "', " +
                    "name = '" + name + "', " +
                    "color = '" + color + "' " +
                    " WHERE id = '" + id + "';";
            try {
                db.execSQL(execSting);
            } catch (SQLException ignored) {
                db.close();
                return false;
            }
            db.close();
            getUsers();
            return (Util.phones.contains(phone));
        } else {
            return false;
        }
    }

    public boolean deleteUser(int id) {
        // Метод удаляет запись пользователя в БД и обновляет структуры.
        // Возвращает true, если успешно и false, если нет.
        if (Util.id2phone.containsKey(id)) {
            String phone = Util.id2phone.get(id);
            SQLiteDatabase db = getReadableDatabase();
            String execSting1 = "DELETE FROM users WHERE id = '" + id + "';";
            String execSting2 = "DELETE FROM points WHERE phone = '" + phone + "';";
            try {
                db.execSQL(execSting1);
                db.execSQL(execSting2);
            } catch (SQLException ignored) {
                db.close();
                return false;
            }
            db.close();
            getUsers();
            return (!Util.id2phone.containsKey(id));
        } else {
            return false;
        }
    }

    public void setLastPoint(PointRecord record) {
        // Метод заносит в таблицу points последние известные координаты пользователя.
        if (Util.phones.contains(record.phone)) {
            String latitude = String.format(Locale.US, PointRecord.FORMAT_DOUBLE, record.latitude);
            String longitude = String.format(Locale.US, PointRecord.FORMAT_DOUBLE, record.longitude);
            if ((record.latitude > -90) && (record.latitude < 90) && (record.longitude > -180) && (record.longitude < 180)) {
                SQLiteDatabase db = getReadableDatabase();
                String execSting = "INSERT OR REPLACE INTO points (phone, latitude, longitude, datetime)" +
                        " VALUES ('" + record.phone + "', '" +
                        latitude + "', '" + longitude + "', '" +
                        record.datetime + "');";
                try {
                    db.execSQL(execSting);
                } catch (SQLException ignored) {
                    db.close();
                    return;
                }
                db.close();
            }
        }
    }

    public PointRecord getLastPoint(String phone) {
        PointRecord record = new PointRecord();
        // Метод возвращает PointRecord по заданному телефону, или null, если нет или неправильно.
        if (Util.phones.contains(phone)) {
            SQLiteDatabase db = getReadableDatabase();
            String execSting = "SELECT * FROM points WHERE phone = '" + phone + "';";
            Cursor cursor = db.rawQuery(execSting, null);
            if (cursor.moveToFirst()) {
                record.phone =
                        cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                record.latitude = Double.parseDouble(
                        cursor.getString(cursor.getColumnIndexOrThrow("latitude")));
                record.longitude = Double.parseDouble(
                        cursor.getString(cursor.getColumnIndexOrThrow("longitude")));
                record.datetime =
                        cursor.getString(cursor.getColumnIndexOrThrow("datetime"));
                if ((record.latitude < -90) || (record.latitude > 90) || (record.longitude < -180) || (record.longitude > 180)) {
                    record = null;
                }
            } else {
                record = null;
            }
            cursor.close();
            db.close();
            return record;
        }
        return null;
    }

}