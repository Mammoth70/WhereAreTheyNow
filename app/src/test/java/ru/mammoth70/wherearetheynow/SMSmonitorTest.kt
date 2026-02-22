package ru.mammoth70.wherearetheynow

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource

class SMSmonitorTest {

    private val monitor = SMSmonitor()

    @ParameterizedTest(name = "{index} => {6}")
    @DisplayName("Тестирование функции разбора SMS сообщений")
    @CsvFileSource(resources = ["/sms_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `parseSms validation test`(
        phone: String,
        message: String,
        expectedLat: Double,
        expectedLon: Double,
        expectedTime: String?,
        isValid: Boolean,
        description: String,
    ) {

        // Запускаем тестируемую функцию.
        val result = monitor.parseSMS(phone, message)

        if (isValid) {
            // Сначала проверяем, что объект вообще создался.
            assertNotNull(result,  "Объект не должен быть null для варианта $description")

            // Теперь проверяем поля.
            assertAll(
                "Проверка полей PointRecord для варианта $description",
                {assertEquals(phone, result.phone, "Неверный номер телефона")},
                {assertEquals(expectedLat, result.latitude, 0.000001, "Не совпадает широта")},
                {assertEquals(expectedLon, result.longitude, 0.000001, "Не совпадает долгота")},
                {assertEquals(expectedTime, result.dateTime, "Не совпадает время")},)
        } else {
            assertNull(result, "Ожидался null для невалидного варианта $description")
        }
    }
}