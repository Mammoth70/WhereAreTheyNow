package ru.mammoth70.wherearetheynow

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource

class SMSmonitorTest {

    private val monitor = SMSmonitor()

    @ParameterizedTest(name = "{index} {6}")
    @DisplayName("Тестирование проверки SMS сообщений")
    @CsvFileSource(resources = ["/sms_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `ParseSms validation test`(
        phone: String,
        message: String,
        expectedLat: Double,
        expectedLon: Double,
        expectedTime: String?,
        shouldBeObj: Boolean,
        description: String,
    ) {
        val result = monitor.parseSMS(phone, message)

        // Проверка корректности (сравнение ожиданий с реальностью).
        if (shouldBeObj) {
            assertNotNull(result,  "Ошибка в тесте '$description': ожидался объект")

            // Проверка всех полей.
            assertEquals(phone, result.phone, "Неверный номер телефона")
            assertEquals(expectedLat, result.latitude, 0.000001, "Широта не совпадает")
            assertEquals(expectedLon, result.longitude, 0.000001, "Долгота не совпадает")
            assertEquals(expectedTime, result.dateTime, "Время не совпадает")
        } else {
            assertNull(result, "Ошибка в тесте '$description': ожидался null")
        }
    }
}