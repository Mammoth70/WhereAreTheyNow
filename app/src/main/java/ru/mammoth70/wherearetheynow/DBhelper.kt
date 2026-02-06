package ru.mammoth70.wherearetheynow

import android.content.ContentValues
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

    fun readDbAllUsers(): List<User> {
        // Функция считывает из БД данные контактов и координат.

        val userList = mutableListOf<User>()
        val query = """
        SELECT u.id, u.phone, u.name, u.color, p.latitude, p.longitude, p.datetime 
        FROM users u 
        LEFT JOIN points p ON u.phone = p.phone
    """.trimIndent()

        try {
            readableDatabase.rawQuery(query, null).use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow("id")
                val phoneIdx = cursor.getColumnIndexOrThrow("phone")
                val nameIdx = cursor.getColumnIndexOrThrow("name")
                val colorIdx = cursor.getColumnIndexOrThrow("color")
                val latIdx = cursor.getColumnIndexOrThrow("latitude")
                val lonIdx = cursor.getColumnIndexOrThrow("longitude")
                val dateIdx = cursor.getColumnIndexOrThrow("datetime")

                while (cursor.moveToNext()) {
                    val phone = cursor.getString(phoneIdx) ?: continue
                    val rawLat = cursor.getString(latIdx)
                    val rawLon = cursor.getString(lonIdx)
                    val dateTime = cursor.getString(dateIdx)

                    var lastRecord: PointRecord? = null

                    if (rawLat != null && rawLon != null && dateTime != null) {
                        val lat = rawLat.toDouble()
                        val lon = rawLon.toDouble()

                        if (lat in -90.0..90.0 && lon in -180.0..180.0) {
                            lastRecord = PointRecord(phone, lat, lon, dateTime)
                        }
                    }

                    val user = User(
                        id = cursor.getInt(idIdx),
                        phone = phone,
                        name = cursor.getString(nameIdx) ?: "",
                        color = cursor.getString(colorIdx) ?: "",
                        lastRecord = lastRecord
                    )
                    userList.add(user)
                }
            }
        } catch (e: NumberFormatException) {
            LogSmart.e("DBhelper", "NumberFormatException в readDbAllUsers()", e)
        } catch (e: SQLException) {
            LogSmart.e("DBhelper", "SQLException в readDbAllUsers()", e)
        }
        return userList
    }

    fun readDbMenuUsers(): List<String> {
        // Функция возвращает список телефонов, отсортированный по id ASC,
        // которые имеют координаты, отсортированные по дате DESC, и по id ASC,
        // и ограничены заданным (10-ю) количеством записей.
        // Функция используется для построения меню.

        val list = mutableListOf<String>()
        val query = """
            SELECT users.phone AS phone_result FROM
                (SELECT points.phone AS phone2
                FROM points INNER JOIN users ON points.phone = users.phone
                ORDER BY points.datetime DESC, users.id ASC LIMIT 10 OFFSET 0) AS tmp
                INNER JOIN users ON tmp.phone2 = users.phone ORDER BY users.id;
            """.trimIndent()
        try {
            readableDatabase.rawQuery(query, null).use { cursor ->
                val phoneIdx = cursor.getColumnIndexOrThrow("phone_result")
                while (cursor.moveToNext()) {
                    cursor.getString(phoneIdx)?.let { list.add(it) }
                }
            }
        } catch (e: SQLException) {
            LogSmart.e("DBhelper", "SQLException в readDbMenuUsers()", e)
        }
        return list
    }

    fun addDbUser(user: User): Long {
        // Функция добавляет контакт в БД.

        val values = ContentValues().apply {
            put("phone", user.phone)
            put("name", user.name)
            put("color", user.color)
        }

        return try {
            writableDatabase.insertWithOnConflict(
                "users",
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )
        } catch (e: SQLException) {
            LogSmart.e("DBhelper", "SQLException в addDbUser($user)", e)
            -1L
        }
    }

    fun editDbUser(user: User): Boolean {
        // Функция меняет контакт в БД.

        val values = ContentValues().apply {
            put("phone", user.phone)
            put("name", user.name)
            put("color", user.color)
        }

        return try {
            val rowsAffected = writableDatabase.update(
                "users",
                values,
                "id = ?",
                arrayOf(user.id.toString())
            )

            if (rowsAffected > 0) {
                checkDbRecords() // Чистим "висящие" точки в БД
                true
            } else {
                false
            }
        } catch (e: SQLException) {
            LogSmart.e("DBhelper", "SQLException в editDbUser($user)", e)
            false
        }
    }

    fun deleteDbUser(user: User): Boolean {
        // Функция удаляет контакт из БД.

        return try {
            writableDatabase.transaction {
                // Удаляем пользователя
                delete("users", "id = ?", arrayOf(user.id.toString()))
                // Удаляем его координаты
                delete("points", "phone = ?", arrayOf(user.phone))
            }
            // Если транзакция завершилась без ошибок — успех
            true
        } catch (e: SQLException) {
            LogSmart.e("DBhelper", "SQLException в deleteDbUser($user)", e)
            false
        }
    }

    fun writeDbLastPoint(record: PointRecord) {
        // Функция заносит в БД последние известные координаты контакта.

        if (record.latitude !in -90.0..90.0 || record.longitude !in -180.0..180.0) {
            return
        }

        val values = ContentValues().apply {
            put("phone", record.phone)
            put("latitude", String.format(Locale.US, PointRecord.FORMAT_DOUBLE, record.latitude))
            put("longitude", String.format(Locale.US, PointRecord.FORMAT_DOUBLE, record.longitude))
            put("datetime", record.dateTime)
        }

        try {
            writableDatabase.replace("points", null, values)
        } catch (e: SQLException) {
            LogSmart.e("DBhelper", "SQLException в writeDbLastPoint($record)", e)
        }
    }

    fun readDbLastAnswer(): PointRecord? {
        // Функция считывает из БД последнюю запись и возвращает PointRecord.
        // Возвращает null, если записи нет, или координаты выходят за допустимые границы.

        val query = "SELECT * FROM points ORDER BY datetime DESC LIMIT 1;"

        return readableDatabase.rawQuery(query, null).use { cursor ->
            val phoneIdx = cursor.getColumnIndexOrThrow("phone")
            val latIdx = cursor.getColumnIndexOrThrow("latitude")
            val lonIdx = cursor.getColumnIndexOrThrow("longitude")
            val dateIdx = cursor.getColumnIndexOrThrow("datetime")

            if (cursor.moveToFirst()) {
                try {
                    val record = PointRecord(
                        cursor.getString(phoneIdx),
                        cursor.getString(latIdx).toDouble(),
                        cursor.getString(lonIdx).toDouble(),
                        cursor.getString(dateIdx)
                    )

                    if (record.latitude !in -90.0..90.0 || record.longitude !in -180.0..180.0) {
                        null
                    } else { record }
                } catch (e: SQLException) {
                    LogSmart.e("DBhelper", "SQLException в readDbLastAnswer()", e)
                    null
                }
            } else { null }
        }
    }

    fun checkDbRecords() {
        // Функция удаляет из таблицы points записи, не имеющие ссылок на таблицу phones.
        // Это предотвращает накопление "мусорных" данных.

        try {
            writableDatabase.delete(
                "points",
                "phone NOT IN (SELECT phone FROM users)",
                null
            )
        } catch (e: SQLException) {
            LogSmart.e("DBhelper", "SQLException в checkDbRecords()", e)
        }
    }

}