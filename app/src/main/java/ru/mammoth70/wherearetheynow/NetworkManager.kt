package ru.mammoth70.wherearetheynow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

object NetworkManager {
// Объект обеспечивает работу с интернет-сервером.


    private val REGEXP_PHONE = Pattern.compile("^\\+?\\d+$")
    private val REGEXP_DOMAIN = Pattern.compile(
        "^(([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])\\.)+([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])(:\\d{1,5})?$"
)
    private val REGEXP_TOKEN = Pattern.compile("^[0-9a-fA-F]{64}$")

    enum class HttpMethod {
        // Класс перечисления методов запроса.
        GET, POST
    }

    private suspend fun fetchJson(url: String, token: String? = null, json: String? = null,
                          requestMethod: HttpMethod = HttpMethod.POST):
            Result<String> = withContext(Dispatchers.IO) {
        // Функция посылает JSON указанным методом по указанному URL и возвращает строку JSON с ответом.
        // Выполнение гарантированно происходит в фоновом потоке.
        var connection: HttpURLConnection? = null
        try {
            val formattedUrl = if (url.startsWith("https://")) {
                url
            } else {
                "https://$url"
            }
            connection = URL(formattedUrl).openConnection() as HttpURLConnection
            connection.requestMethod = requestMethod.name
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Accept", "application/json")
            if (!token.isNullOrEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }

            if (!json.isNullOrEmpty()) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { os ->
                    val input = json.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }
            }

            val responseCode = try {
                connection.responseCode
            } catch (_: Exception) {
                -1
            }
            if (responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Result.success(response)
            } else {
                val errorResponse = if (responseCode != -1) {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                } else {
                    null
                }
                val errorMessage = errorResponse ?: "$responseCode"
                Result.failure(Exception(errorMessage))            }
        } catch (e: Exception) {
            LogSmart.e("NetworkManager", "Exception в fetchJson($url, $json)", e)
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }


    fun fetchJsonAsync(url: String, token: String? = null,  json: String? = null,
                       requestMethod: HttpMethod = HttpMethod.POST,
                       onFinished: (() -> Unit)? = null,
                       onResult: ((Result<String>) -> Unit)? = null
        // Функция посылает JSON указанным методом по указанному URL и возвращает коллбеком строку JSON с ответом.
    ) {

        if (!isInternetAvailable()) {
            onResult?.invoke(Result.failure(Exception("No internet connection")))
            onFinished?.invoke()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = fetchJson(url, token, json, requestMethod)
                onResult?.invoke(result)
            } catch (e: Exception) {
                onResult?.invoke(Result.failure(e))
            } finally {
                onFinished?.invoke()
            }
        }
    }


    fun parseActivationJson(json: String): Triple<String, String, String>? {
         // Функция разбирает JSON-строку активации и проверяет статус.
         // Возвращает Triple(server, phone, api_token) в случае успеха или null, если статус не success.
        return try {
            val jsonObject = JSONObject(json)
            val status = jsonObject.optString("status")
            if (status != "success") return null

            val server = jsonObject.optString("server").trim().lowercase()
            val phone = jsonObject.optString("phone").trim()
            val apiToken = jsonObject.optString("api_token").trim().lowercase()

            if (server.isBlank() || phone.isBlank() || apiToken.isBlank()) return null

            if (!REGEXP_DOMAIN.matcher(server).matches()) return null

            if (!REGEXP_PHONE.matcher(phone).matches()) return null

            if (!REGEXP_TOKEN.matcher(apiToken).matches()) return null

            Triple(server, phone, apiToken)
        } catch (e: Exception) {
            LogSmart.e("NetworkManager", "Exception в parseActivationJson($json)", e)
            null
        }
    }

}