package ru.mammoth70.wherearetheynow

data class User(
    // Класс данных для хранения полей контакта (id, телефона, имя, цвет метки на карте и время последнего получения координат).
    val id: Long,
    val phone: String,
    val name: String,
    val color: String,
    var lastRecord: PointRecord? = null,
)