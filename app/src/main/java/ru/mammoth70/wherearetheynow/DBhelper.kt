package ru.mammoth70.wherearetheynow

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import java.util.Locale

class DBhelper(context: Context?) : SQLiteOpenHelper(context, "watnDB",
    null, DB_VERSION) {
    // Класс обслуживает базу данных со списком контактов и прочими структурами контактов.

    companion object {
        private const val DB_VERSION = 3 // версия БД
    }

    private val createTableUsersSting = "CREATE TABLE IF NOT EXISTS users " +
            "(id integer PRIMARY KEY AUTOINCREMENT, phone text UNIQUE, name text, color text);"
    private val createTablePointsSting = "CREATE TABLE IF NOT EXISTS points " +
            "(phone text UNIQUE, latitude text, longitude text, datetime text);"

    override fun onCreate(db: SQLiteDatabase) {
        // Функция создаёт таблицы users и points.
        db.execSQL(createTableUsersSting)
        db.execSQL(createTablePointsSting)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Функция делает апгрейд БД.
        if (oldVersion == 1 && newVersion > 1) {
            // Удаляем из таблицы users поле label.
            // Снимаем ограничение на уникальность поля name в таблице users.
            // Добавляем таблицу points.
            db.transaction {
                try {
                    execSQL(
                        "CREATE TEMPORARY TABLE users_temp " +
                                "(id integer, phone text, name text, color text);"
                    )
                    execSQL("INSERT INTO users_temp SELECT id, phone, name, color FROM users;")
                    execSQL("DROP TABLE users;")
                    execSQL(createTableUsersSting)
                    execSQL("INSERT INTO users SELECT id, phone, name, color FROM users_temp;")
                    execSQL("DROP TABLE users_temp;")
                    execSQL(createTablePointsSting)
                } finally {
                }
            }
        }
        if (oldVersion == 2 && newVersion > 2) {
            // Добавляем таблицу points.
            db.transaction {
                try {
                    execSQL(createTablePointsSting)
                } finally {
                }
            }
        }
    }

    fun readUsers() {
            // Функция считывает список разрешенных телефонов и словари контактов из БД.
            readableDatabase.use { db ->
                db.rawQuery("SELECT * FROM users;", null).use { cursor ->
                    Util.phones.clear()
                    Util.id2phone.clear()
                    Util.phone2id.clear()
                    Util.phone2name.clear()
                    Util.phone2color.clear()
                    Util.phone2record.clear()
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(
                            cursor.getColumnIndexOrThrow("id"))
                        val phone =
                            cursor.getString(
                            cursor.getColumnIndexOrThrow("phone"))
                        val name =
                            cursor.getString(
                            cursor.getColumnIndexOrThrow("name"))
                        val color =
                            cursor.getString(
                            cursor.getColumnIndexOrThrow("color"))
                        Util.phones.add(phone!!)
                        Util.id2phone.put(id, phone)
                        Util.phone2id.put(phone, id)
                        Util.phone2name.put(phone, name!!)
                        Util.phone2color.put(phone, color!!)
                        val record = getLastPoint(phone)
                        if (record != null) {
                            Util.phone2record.put(phone, record)
                        }
                    }
                }
            }
        }

    fun readMenuUsers() {
            // Функция считывает список телефонов, отортированный по id ASC,
            // имеющих координаты, отсортированные по дате DESC, и по id ASC,
            // и ограниченные заданным (10-ю) количеством записей.
            // Используется для построения меню.
            Util.menuPhones.clear()
            val execSting =
                "SELECT users.phone AS phone1 FROM " +
                    "(SELECT points.phone AS phone2 " +
                    "FROM points INNER JOIN users ON points.phone = users.phone " +
                    "ORDER BY points.datetime DESC, users.id ASC LIMIT 10 OFFSET 0) AS tmp " +
                    "INNER JOIN users ON tmp.phone2 = users.phone ORDER BY users.id;"
            readableDatabase.use { db ->
                db.rawQuery(execSting, null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val phone =
                            cursor.getString(
                                cursor.getColumnIndexOrThrow("phone1"))
                        Util.menuPhones.add(phone!!)
                    }
                }
            }
        }

    fun addUser(phone: String?, name: String?, color: String?): Boolean {
        // Функция добавляет запись контакта в БД и обновляет структуры.
        // Возвращает true, если успешно и false, если нет.
        if (!Util.phones.contains(phone)) {
            val execSting = "INSERT OR IGNORE INTO users (phone, name, color)" +
                    " VALUES ('$phone', '$name', '$color');"
            readableDatabase.use { db ->
                try {
                    db.execSQL(execSting)
                } catch (_: SQLException) {
                    return false
                }
            }
            readUsers()
            return (Util.phones.contains(phone))
        } else {
            return false
        }
    }

    fun editUser(id: Int, phone: String?, name: String?, color: String?): Boolean {
        // Функция изменяет запись контакта в БД и обновляет структуры.
        // Возвращает true, если успешно и false, если нет.
        if (Util.id2phone.containsKey(id)) {
            val execSting = "UPDATE users SET " +
                    "phone = '$phone', name = '$name', color = '$color' WHERE id = '$id';"
            readableDatabase.use { db ->
                try {
                    db.execSQL(execSting)
                } catch (_: SQLException) {
                    return false
                }
            }
            readUsers()
            return (Util.phones.contains(phone))
        } else {
            return false
        }
    }

    fun deleteUser(id: Int): Boolean {
        // Функция удаляет запись контакта в БД и обновляет структуры.
        // Возвращает true, если успешно и false, если нет.
        if (Util.id2phone.containsKey(id)) {
            val phone = Util.id2phone[id]
            readableDatabase.use { db ->
                try {
                    db.execSQL("DELETE FROM users WHERE id = '$id';")
                    db.execSQL("DELETE FROM points WHERE phone = '$phone';")
                } catch (_: SQLException) {
                    return false
                }
            }
            readUsers()
            return (!Util.id2phone.containsKey(id))
        } else {
            return false
        }
    }

    fun setLastPoint(record: PointRecord) {
        // Функция заносит в таблицу points последние известные координаты контакта.
        if (Util.phones.contains(record.phone)) {
            if ((record.latitude > -90) && (record.latitude < 90) &&
                (record.longitude > -180) && (record.longitude < 180)
            ) {
                val latitude = String.format(Locale.US,
                        PointRecord.FORMAT_DOUBLE, record.latitude)
                val longitude = String.format(Locale.US,
                        PointRecord.FORMAT_DOUBLE, record.longitude)
                val execSting =
                        "INSERT OR REPLACE INTO points (phone, latitude, longitude, datetime)" +
                        " VALUES ('$record.phone', '$latitude', '$longitude', '$record.dateTime');"
                readableDatabase.use { db ->
                    try {
                        db.execSQL(execSting)
                    } catch (_: SQLException) {
                    }
                }
            }
        }
    }

    fun getLastPoint(phone: String): PointRecord? {
        // Функция возвращает PointRecord по заданному телефону, или null, если нет или неправильно.
        if (Util.phones.contains(phone)) {
            val execSting = "SELECT * FROM points WHERE phone = '$phone';"
            readableDatabase.use { db ->
                db.rawQuery(execSting, null).use { cursor ->
                    if (cursor.moveToFirst()) {
                        val record = PointRecord(
                            cursor.getString(
                                cursor.getColumnIndexOrThrow("phone")),
                            cursor.getString(
                                cursor.getColumnIndexOrThrow("latitude"))
                                .toDouble(),
                            cursor.getString(
                                cursor.getColumnIndexOrThrow("longitude"))
                                .toDouble(),
                            cursor.getString(
                                cursor.getColumnIndexOrThrow("datetime"))
                        )
                        return if ((record.latitude > -90) && (record.latitude < 90) &&
                            (record.longitude > -180) && (record.longitude < 180)
                        ) {
                            record
                        } else {
                            null
                        }
                    }
                }
            }
        }
        return null
    }

}