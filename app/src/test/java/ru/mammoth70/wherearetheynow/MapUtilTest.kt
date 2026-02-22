package ru.mammoth70.wherearetheynow

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import java.util.*

class MapUtilTest {

    private val fixedNow: Date = Calendar.getInstance().apply {
        set(2025, Calendar.OCTOBER, 10, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    @ParameterizedTest(name = "{index} => {2}")
    @DisplayName("Тестирование функции вывода текстом разности во времени")
    @CsvFileSource(resources = ["/time_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `timePassed validation test`(
        smsDate: String?,
        resKey: String,
        description: String,
    ) {

        // Превращаем строковый ключ из CSV в реальный ID ресурса.
        val expectedResId = when (resKey) {
            "now" -> R.string.now
            "minutes_ago" -> R.string.minutes_ago
            "hours_ago" -> R.string.hours_ago
            "today" -> R.string.today
            "yesterday" -> R.string.yesterday
            "before_yesterday" -> R.string.before_yesterday
            "long_ago" -> R.string.long_ago
            else -> 0
        }

        // Обрабатываем "null" из CSV.
        val cleanDate = if (smsDate == "null") null else smsDate

        // Запускаем тестируемую функцию.
        val result = calculateTimePassed(
            dateTime = cleanDate,
            now = fixedNow,
            getString = { resId, arg ->
                if (arg != null) "$resId $arg" else "$resId"
            }
        )

        val expected = if (expectedResId == 0) "" else {
            when (expectedResId) {
                // Если в CSV указано "minutes_ago", рассчитываем минуты для сравнения.
                R.string.minutes_ago -> {
                    val dateSMS = stringToDate(cleanDate!!)!!
                    val diff = (fixedNow.time - dateSMS.time) / 60000
                    "$expectedResId $diff"
                }
                // Если в CSV указано "hours_ago", рассчитываем часы для сравнения.
                R.string.hours_ago -> {
                    val dateSMS = stringToDate(cleanDate!!)!!
                    val diff = (fixedNow.time - dateSMS.time) / 3600000
                    "$expectedResId $diff"
                }
                // Для остальных случаев (сегодня, вчера и т.д.) аргументов нет.
                else -> "$expectedResId"
            }
        }

        assertEquals(expected, result, "Ошибка в варианте $description")
    }
}