package ru.mammoth70.wherearetheynow

import org.json.JSONObject
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource

class CheckJsonPointRecordTest  {

    @ParameterizedTest(name = "{index} => {6}")
    @DisplayName("Тестирование функции проверки JSON сообщений")
    @CsvFileSource(resources = ["/json_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `CheckJsonPointRecord validation test`(
        phone: String,
        json: String,
        expectedLat: Double,
        expectedLon: Double,
        expectedTime: String?,
        isValid: Boolean,
        description: String,
    ) {

        // Достаем данные как строки
        val cleanJson = json.trim()
        val validJson = cleanJson.replace("'", "\"")
        val jsonObject = JSONObject(validJson)
        val latitude = jsonObject.optString("latitude")     // "55.774266"
        val longitude = jsonObject.optString("longitude")    // "37.483221"
        val dateTime = jsonObject.optString("created_at")

        // Запускаем тестируемую функцию.
        val result = checkJsonPointRecord(phone, latitude, longitude, dateTime)

        if (isValid) {
            // Сначала проверяем, что объект вообще создался.
            assertNotNull(result,  "Объект не должен быть null для варианта $description")

            // Теперь проверяем поля.
            assertAll(
                "Проверка полей PointRecord для варианта $description",
                {assertEquals(phone, result.phone, "Не совпадает номер телефона")},
                {assertEquals(expectedLat, result.latitude, 0.000001, "Не совпадает широта")},
                {assertEquals(expectedLon, result.longitude, 0.000001, "Не совпадает долгота")},
                {assertEquals(expectedTime, result.dateTime, "Не совпадает время")},)
        } else {
            assertNull(result, "Ожидался null для невалидного варианта $description, но пришло: $result")
        }
    }
}