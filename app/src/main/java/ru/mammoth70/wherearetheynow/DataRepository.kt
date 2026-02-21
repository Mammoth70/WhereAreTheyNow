package ru.mammoth70.wherearetheynow

import androidx.core.content.edit
import ru.mammoth70.wherearetheynow.SettingsManager.NAME_SETTINGS


object DataRepository {
    // Основной список объектов (единственный источник истины).


    private const val NAME_MY_PHONE = "myphone"

    private val _users = mutableListOf<User>()        // Список контактов с телефонами.
    val users: List<User> get() = synchronized(this) { _users.toList() }

    private val _menuPhones = mutableListOf<String>() // Список телефонов для меню. Ограничен наличием записей геолокации.
    val menuPhones: List<String> get() = synchronized(this) { _menuPhones.toList() }

    private val phoneMap = HashMap<String, User>()    // Индекс (поиск по номеру телефона за O(1)).
    private val idMap = HashMap<Int, User>()          // Индекс (поиск по id за O(1)).


    var lastAnswerRecord: PointRecord? = null         // Запись с данными последнего ответа.
        get() = synchronized(this) { field }
        private set


    var myPhone: String = ""
        get() = synchronized(this) {        // Номер моего телефона.
            if (field.isEmpty()) {
                val settings = App.appContext.getSharedPreferences(NAME_SETTINGS, android.content.Context.MODE_PRIVATE)
                field = settings.getString(NAME_MY_PHONE, "") ?: ""
            }
            field
        }
        set(value) = synchronized(this) {
            if (value.isNotBlank()) {
                field = value
                val settings = App.appContext.getSharedPreferences(NAME_SETTINGS, android.content.Context.MODE_PRIVATE)
                settings.edit { putString(NAME_MY_PHONE, value) }
            }
        }


    fun refreshData() {
        // Функция считывает из БД все данные.

        val freshUsers = DBhelper.dbHelper.readDbAllUsers()
        val freshMenu = DBhelper.dbHelper.readDbMenuUsers()
        val lastAnswer = DBhelper.dbHelper.readDbLastAnswer()

        synchronized(this) {
            _users.clear()
            _users.addAll(freshUsers)

            _menuPhones.clear()
            _menuPhones.addAll(freshMenu)

            lastAnswerRecord = lastAnswer

            phoneMap.clear()
            phoneMap.putAll(freshUsers.associateBy { it.phone })

            idMap.clear()
            idMap.putAll(freshUsers.associateBy { it.id })
        }
    }


    fun addUser(phone: String, name: String, color: String): Boolean {
        // Функция для добавления контакта.
        // Возвращает true, если успешно и false, если нет.

        if (phone.isEmpty()) return false
        if (name.isEmpty()) return false
        if (!PinColors.isValidColors(color)) return false

        synchronized(this) {
            if (phoneMap.containsKey(phone)) {
                return false
            }
        }

        val newId = DBhelper.dbHelper.addDbUser(User(-1, phone, name, color))
        if (newId != -1L) {
            val newUser = User(newId.toInt(), phone, name, color)
            synchronized(this) {
                if (phoneMap.containsKey(phone)) return false
                _users.add(newUser)
                phoneMap[phone] = newUser
                idMap[newUser.id] = newUser
                return true
            }
        }
        return false
    }


    fun deleteUser(id: Int): Boolean {
        // Функция для добавления контакта.
        // Возвращает true, если успешно и false, если нет.

        val userToDelete = synchronized(this) {
            idMap[id]
        } ?: return false

        if (DBhelper.dbHelper.deleteDbUser(userToDelete)) {
            val freshLastAnswer = DBhelper.dbHelper.readDbLastAnswer()
            val freshMenu = DBhelper.dbHelper.readDbMenuUsers()

            synchronized(this) {
                _users.remove(userToDelete)
                idMap.remove(id)
                phoneMap.remove(userToDelete.phone)

                lastAnswerRecord = freshLastAnswer

                _menuPhones.clear()
                _menuPhones.addAll(freshMenu)
            }
            return true
        }

        return false
    }


    fun editUser(user: User): Boolean {
        // Функция для редактирования контакта.
        // Возвращает true, если успешно и false, если нет.

        if (user.phone.isBlank() || user.name.isBlank()) return false
        if (!PinColors.isValidColors(user.color)) return false

        synchronized(this) {
            val oldUser = idMap[user.id] ?: return false

            if (oldUser.phone != user.phone && phoneMap.containsKey(user.phone)) {
                return false
            }
        }

        if (DBhelper.dbHelper.editDbUser(user)) {
            val freshLastAnswer = DBhelper.dbHelper.readDbLastAnswer()
            val freshMenu = DBhelper.dbHelper.readDbMenuUsers()

            synchronized(this) {
                val currentOldUser = idMap[user.id] ?: return false

                val updatedUser = currentOldUser.copy(
                    phone = user.phone,
                    name = user.name,
                    color = user.color
                )

                val index = _users.indexOfFirst { it.id == user.id }
                if (index != -1) {
                    _users[index] = updatedUser
                }

                if (currentOldUser.phone != user.phone) {
                    phoneMap.remove(currentOldUser.phone)
                }

                phoneMap[user.phone] = updatedUser

                idMap[user.id] = updatedUser

                lastAnswerRecord = freshLastAnswer

                _menuPhones.clear()
                _menuPhones.addAll(freshMenu)
            }
            return true
        }

        return false
    }


    fun writeLastPoint(record: PointRecord): Boolean {
        // Функция заносит в память и в БД последние известные координаты контакта.

        DBhelper.dbHelper.writeDbLastPoint(record)
        val freshMenu = DBhelper.dbHelper.readDbMenuUsers()

        synchronized(this) {
            val oldUser = phoneMap[record.phone] ?: return false

            val updatedUser = oldUser.copy(lastRecord = record)

            val index = _users.indexOfFirst { it.phone == record.phone }
            if (index != -1) {
                _users[index] = updatedUser
            }

            phoneMap[record.phone] = updatedUser
            idMap[updatedUser.id] = updatedUser

            lastAnswerRecord = record
            _menuPhones.clear()
            _menuPhones.addAll(freshMenu)
        }

        return true
    }


    // Методы доступа, заменяющие мапы.
    fun getUser(phone: String) = synchronized(this) { phoneMap[phone] }
    fun getUser(id: Int): User? = synchronized(this) { idMap[id] }
}