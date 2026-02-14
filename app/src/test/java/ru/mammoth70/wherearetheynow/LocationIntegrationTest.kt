package ru.mammoth70.wherearetheynow

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationIntegrationTest {

    private val sender = GetLocation()
    private val monitor = SMSmonitor()
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    @ParameterizedTest(name = "{index} => {2}")
    @DisplayName("Интеграционное тестирование логики кодирования и декодирования координат и времени")
    @CsvFileSource(resources = ["/location_integration.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `integration test format to parse`(
        testLat: Double,
        testLon: Double,
        description: String
    ) {
        val phone = "+70000000000"
        val testDates = listOf(
            Date(),                                      // текущее время
            Date(0L),                                    // 1970-01-01 00:00:00 (начало времён)
            sdf.parse("0001-01-01 00:00:00")!!, // начало нашей эры
            sdf.parse("2000-01-01 00:00:00")!!, // миллениум
            sdf.parse("2024-02-29 12:00:00")!!, // високосный год
            sdf.parse("2024-02-29 23:59:59")!!, // високосный день (граница февраля)
            sdf.parse("2026-05-05 05:05:05")!!, // проверка ведущих нулей во всех полях, кроме года
            sdf.parse("2026-01-01 00:00:00")!!, // начало года
            sdf.parse("2026-02-28 23:59:59")!!, // конец февраля
            sdf.parse("2026-12-31 23:59:59")!!, // конец года
            sdf.parse("1010-10-10 10:10:10")!!, // повторяющиеся цифры
            sdf.parse("2049-12-31 23:59:59")!!, // недалёкое будущее
            sdf.parse("2099-12-31 23:59:59")!!, // граница XXI-го века
            sdf.parse("9999-12-31 23:59:59")!!, // привет, Азимов
        )

        testDates.forEach { testDate ->

            // Тестируем оба режима: Answer (false) и Request (true).
            listOf(true, false).forEach { isRequest ->

                // Кодируем помаленьку.
                val smsText = sender.formatLocation(testLat, testLon, testDate, isRequest)
                assertNotNull(smsText, "Ошибка формирования SMS (тест: $description)")

                // Декодируем.
                val result = monitor.parseSMS(phone, smsText)

                // Проверяем целостность данных.
                assertNotNull(result, "Парсер не смог разобрать SMS: $smsText (тест: $description)")
                assertEquals(testLat, result.latitude, 0.000001, "Широта исказилась")
                assertEquals(testLon, result.longitude, 0.000001, "Долгота исказилась")
                assertEquals(sdf.format(testDate), result.dateTime, "Дата/время исказились")
            }
        }
    }
}