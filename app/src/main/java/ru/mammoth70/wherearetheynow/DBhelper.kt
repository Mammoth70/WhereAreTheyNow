package ru.mammoth70.wherearetheynow

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import ru.mammoth70.wherearetheynow.App.Companion.appContext
import java.util.Locale

class DBhelper(context: Context?) : SQLiteOpenHelper(context, "watnDB",
    null, DB_VERSION) {
    // Класс обслуживает базу данных со списком контактов и прочими структурами контактов.

    companion object {
        private const val DB_VERSION = 3 // версия БД
        val dbHelper = DBhelper(appContext)
        private const val CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS users " +
                "(id integer PRIMARY KEY AUTOINCREMENT, phone text UNIQUE, name text, color text);"
        private const val CREATE_TABLE_POINTS = "CREATE TABLE IF NOT EXISTS points " +
                "(phone text UNIQUE, latitude text, longitude text, datetime text);"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Функция создаёт таблицы users и points.
        db.execSQL(CREATE_TABLE_USERS)
        db.execSQL(CREATE_TABLE_POINTS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Функция делает апгрейд БД.
        if (oldVersion == 1 && newVersion > 1) {
            // Удаление из таблицы users поля label.
            // Удаление ограничения на уникальность поля name в таблице users.
            // Добавление таблицы points.
            db.transaction {
                try {
                    execSQL("CREATE TEMPORARY TABLE users_temp (id integer, phone text, name text, color text);")
                    execSQL("INSERT INTO users_temp SELECT id, phone, name, color FROM users;")
                    execSQL("DROP TABLE users;")
                    execSQL(CREATE_TABLE_USERS)
                    execSQL("INSERT INTO users SELECT id, phone, name, color FROM users_temp;")
                    execSQL("DROP TABLE users_temp;")
                    execSQL(CREATE_TABLE_POINTS)
                } finally {
                }
            }
        }
        if (oldVersion == 2 && newVersion > 2) {
            // Добавление таблицы points.
            db.transaction {
                try {
                    execSQL(CREATE_TABLE_POINTS)
                } finally {
                }
            }
        }
    }

    fun readUsers() {
        // Функция считывает из БД список разрешенных телефонов и словари контактов.
        Util.phones.clear()
        Util.id2phone.clear()
        Util.phone2id.clear()
        Util.phone2name.clear()
        Util.phone2color.clear()
        Util.phone2record.clear()
        readableDatabase.use { db ->
            db.rawQuery("SELECT * FROM users;", null).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))!!
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))!!
                    val color = cursor.getString(cursor.getColumnIndexOrThrow("color"))!!
                    Util.phones.add(phone)
                    Util.id2phone[id] = phone
                    Util.phone2id[phone] = id
                    Util.phone2name[phone] = name
                    Util.phone2color[phone] = color
                    readLastPoint(phone)?.let {
                        Util.phone2record.put(phone, it)
                    }
                }
            }
        }
    }

    fun readMenuUsers() {
        // Функция возвращает список телефонов, отсортированный по id ASC,
        // которые имеют координаты, отсортированные по дате DESC, и по id ASC,
        // и ограничены заданным (10-ю) количеством записей.
        // Функция используется для построения меню.
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
                    val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone1"))!!
                    Util.menuPhones.add(phone)
                }
            }
        }
    }

    fun addUser(phone: String?, name: String?, color: String?): Boolean {
        // Функция добавляет запись контакта в БД и обновляет структуры.
        // Возвращает true, если успешно и false, если нет.
        if (phone in Util.phones) {
            return false
        }
        val execSting = "INSERT OR IGNORE INTO users (phone, name, color) VALUES ('$phone', '$name', '$color');"
        readableDatabase.use { db ->
            try {
                db.execSQL(execSting)
            } catch (_: SQLException) {
                return false
            }
        }
        readUsers()
        return (phone in Util.phones)
    }

    fun editUser(id: Int, phone: String?, name: String?, color: String?): Boolean {
        // Функция изменяет запись контакта в БД и обновляет структуры.
        // Возвращает true, если успешно и false, если нет.
        if (id !in Util.id2phone) {
            return false
        }
        val execSting = "UPDATE users SET phone = '$phone', name = '$name', color = '$color' WHERE id = '$id';"
        readableDatabase.use { db ->
            try {
                db.execSQL(execSting)
            } catch (_: SQLException) {
                return false
            }
        }
        checkRecords()
        readUsers()
        Util.lastAnswerRecord = readLastAnswer()
        return (phone in Util.phones)
    }

    fun deleteUser(id: Int): Boolean {
        // Функция удаляет запись контакта в БД и обновляет структуры.
        // Возвращает true, если успешно и false, если нет.
        if (id !in Util.id2phone) {
            return false
        }
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
        Util.lastAnswerRecord = readLastAnswer()
        return (id !in Util.id2phone)
    }

    fun writeLastPoint(record: PointRecord) {
        // Функция заносит в HashMap и в БД последние известные координаты контакта.
        // Функция заносит в record ответ с последними полученными координатами.
        if (record.phone !in Util.phones) {
            return
        }
        if ((record.latitude < -90) || (record.latitude > 90) ||
            (record.longitude < -180) || (record.longitude > 180)) {
            return
        }
        Util.phone2record[record.phone] = record
        Util.lastAnswerRecord = record
        val phone = record.phone
        val latitude = String.format(Locale.US,PointRecord.FORMAT_DOUBLE, record.latitude)
        val longitude = String.format(Locale.US,PointRecord.FORMAT_DOUBLE, record.longitude)
        val dateTime = record.dateTime
        val execSting =
                "INSERT OR REPLACE INTO points (phone, latitude, longitude, datetime)" +
                " VALUES ('$phone', '$latitude', '$longitude', '$dateTime');"
        readableDatabase.use { db ->
            try {
                db.execSQL(execSting)
            } catch (_: SQLException) {
            }
        }
    }

    fun readLastPoint(phone: String): PointRecord? {
        // Функция считывает из БД и возвращает PointRecord по заданному телефону,
        // Возвращает null, если записи нет, или она некорректная.
        if (phone !in Util.phones) {
            return null
        }
        val execSting = "SELECT * FROM points WHERE phone = '$phone';"
        readableDatabase.use { db ->
            db.rawQuery(execSting, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    val record = PointRecord(
                        cursor.getString(
                            cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(
                            cursor.getColumnIndexOrThrow("latitude")).toDouble(),
                        cursor.getString(
                            cursor.getColumnIndexOrThrow("longitude")).toDouble(),
                        cursor.getString(
                            cursor.getColumnIndexOrThrow("datetime"))
                    )
                    return if ((record.latitude < -90) || (record.latitude > 90) ||
                        (record.longitude < -180) || (record.longitude > 180)
                    ) {
                        null
                    } else {
                        record
                    }
                }
            }
        }
        return null
    }

    fun readLastAnswer(): PointRecord? {
        // Функция считывает из БД и возвращает PointRecord с последним запросом,
        // Возвращает null, если записи нет, или она некорректная.
        val execSting = "SELECT * FROM points ORDER BY datetime DESC LIMIT 10 OFFSET 0;"
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
                    return if ((record.latitude < -90) || (record.latitude > 90) ||
                        (record.longitude < -180) || (record.longitude > 180)
                    ) {
                        null
                    } else {
                        record
                    }
                }
            }
        }
        return null
    }

    fun checkRecords() {
        // Функция удаляет из таблицы points записи, не имеющие ссылок на таблицу phones.
        val execSting = "DELETE FROM points WHERE NOT EXISTS " +
                        "(SELECT 1 FROM users WHERE points.phone = users.phone);"
        readableDatabase.use { db ->
            try {
                db.execSQL(execSting)
            } catch (_: SQLException) {
            }
        }
    }

}