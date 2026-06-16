package ru.mammoth70.wherearetheynow

import org.json.JSONObject
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import ru.mammoth70.wherearetheynow.NetworkManager.parseActivationJson

class ParseActivationJsonTest  {

    @ParameterizedTest(name = "{index} => {5}")
    @DisplayName("Тестирование функции проверки JSON сообщений")
    @CsvFileSource(resources = ["/json_activate.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `ParseActivationJson validation test`(
        json: String,
        expectedServer: String?,
        expectedPhone: String?,
        expectedToken: String?,
        isValid: Boolean,
        description: String,
    ) {

        // Достаем данные как строки
        val cleanJson = json.trim()
        val validJson = cleanJson.replace("'", "\"")

        // Запускаем тестируемую функцию.
        val result = parseActivationJson(validJson)

        if (isValid) {
            // Сначала проверяем, что объект вообще создался.
            assertNotNull(result,  "Объект не должен быть null для варианта $description")

            // Теперь проверяем поля.
            val (server, phone, apiToken) = result
            assertAll(
                "Проверка полей triple для варианта $description",
                {assertEquals(expectedServer, server, "Не совпадает сервер")},
                {assertEquals(expectedPhone, phone, "Не совпадает номер телефона")},
                {assertEquals(expectedToken, apiToken, "Не совпадает токен")},)
        } else {
            assertNull(result, "Ожидался null для невалидного варианта $description")
        }
    }
}