package ru.mammoth70.wherearetheynow

import androidx.core.content.edit

private const val NAME_MY_PHONE = "myphone"

object DataRepository {
    // Основной список объектов (единственный источник истины).

    private val _users = mutableListOf<User>() // Список контактов с телефонами.
    val users: List<User> get() = _users

    private val _menuPhones = mutableListOf<String>() // Список телефонов для меню. Ограничен наличием записей геолокации.
    val menuPhones: List<String> get() = _menuPhones

    private val phoneMap = HashMap<String, User>() // Индекс (поиск по номеру телефона за O(1)).
    private val idMap = HashMap<Int, User>()       // Индекс (поиск по id за O(1)).

    var lastAnswerRecord: PointRecord? = null   // Запись с данными последнего ответа.

    var myPhone: String = ""     // Номер моего телефона.
        get() {
            if (field.isEmpty()) {
                val settings = App.appContext.getSharedPreferences(NAME_SETTINGS, android.content.Context.MODE_PRIVATE)
                field = settings.getString(NAME_MY_PHONE, "") ?: ""
            }
            return field
        }
        set(value) {
            if (value.isNotBlank()) {
                field = value
                val settings = App.appContext.getSharedPreferences(NAME_SETTINGS,android.content.Context.MODE_PRIVATE)
                settings.edit { putString(NAME_MY_PHONE, value) }
            }
        }

    fun refreshData() {
        // Функция считывает из БД все данные.

        val freshUsers = DBhelper.dbHelper.readDbAllUsers()
        val freshMenu = DBhelper.dbHelper.readDbMenuUsers()
        val lastAnswer = DBhelper.dbHelper.readDbLastAnswer()

        _users.clear()
        _users.addAll(freshUsers)

        _menuPhones.clear()
        _menuPhones.addAll(freshMenu)

        lastAnswerRecord = lastAnswer

        phoneMap.clear()
        phoneMap.putAll(_users.associateBy { it.phone })

        idMap.clear()
        idMap.putAll(_users.associateBy { it.id })
    }

    fun addUser(phone: String, name: String, color: String): Boolean {
        // Функция для добавления контакта.
        // Возвращает true, если успешно и false, если нет.

        if (phoneMap.containsKey(phone)) {
            return false
        }

        val newId = DBhelper.dbHelper.addDbUser(phone, name, color)
        if (newId != -1L) {
            val newUser = User(newId.toInt(), phone, name, color)
            _users.add(newUser)
            phoneMap[phone] = newUser
            idMap[newUser.id] = newUser
            return true
        }
        return false
    }

    fun deleteUser(id: Int): Boolean {
        // Функция для добавления контакта.
        // Возвращает true, если успешно и false, если нет.

        val user = idMap[id] ?: return false

        if (DBhelper.dbHelper.deleteDbUser(id, user.phone)) {
            _users.remove(user)
            idMap.remove(id)
            phoneMap.remove(user.phone)

            lastAnswerRecord = DBhelper.dbHelper.readDbLastAnswer()

            return true
        }
        return false
    }

    fun editUser(id: Int, phone: String, name: String, color: String): Boolean {
        // Функция для редактирования контакта.
        // Возвращает true, если успешно и false, если нет.

        val oldUser = getUser(id) ?: return false

        if (oldUser.phone != phone && getUser(phone) != null) {
            return false
        }

        if (DBhelper.dbHelper.editDbUser(id, phone, name, color)) {
            val updatedUser = oldUser.copy(
                phone = phone,
                name = name,
                color = color
            )

            val index = users.indexOfFirst { it.id == id }
            if (index != -1) {
                _users[index] = updatedUser
            }

            if (oldUser.phone != phone) {
                phoneMap.remove(oldUser.phone)
            }
            phoneMap[phone] = updatedUser
            idMap[id] = updatedUser

            lastAnswerRecord = DBhelper.dbHelper.readDbLastAnswer()

            return true
        }

        return false
    }

    fun writeLastPoint(record: PointRecord): Boolean {
        // Функция заносит в память и в БД последние известные координаты контакта.

        val user = getUser(record.phone) ?: return false

        DBhelper.dbHelper.writeDbLastPoint(record)

        user.lastRecord = record

        lastAnswerRecord = record

        val freshMenu = DBhelper.dbHelper.readDbMenuUsers()
        _menuPhones.clear()
        _menuPhones.addAll(freshMenu)

        return true
    }

    // Методы доступа, заменяющие мапы.
    fun getUser(phone: String) = phoneMap[phone]
    fun getUser(id: Int) = idMap[id]
}