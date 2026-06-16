package ru.mammoth70.wherearetheynow

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.util.regex.Pattern


object NetworkManager {
// Объект обеспечивает работу с сервером-интернета.


    private val REGEXP_ISO_Z_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$")
    private val REGEXP_PHONE = Pattern.compile("^\\+?\\d+$")
    private val REGEXP_DOMAIN = Pattern.compile(
        "^(([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])\\.)+([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])(:\\d{1,5})?$"
)
    private val REGEXP_TOKEN = Pattern.compile("^[a-f0-9]{64}$")

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

    fun parseAndWriteLocations(json: String) {
        // Функция парсит JSON-строку со списком координат
        // и обновляет данные в DataRepository.
        try {
            val rootObject = JSONObject(json)

            val status = rootObject.optString("status")
            if (status != "success") return

            val dataArray = rootObject.optJSONArray("data") ?: return

            for (i in 0 until dataArray.length()) {
                val deviceObject = dataArray.getJSONObject(i)

                val validRecord = checkJsonPointRecord(
                    deviceObject.optString("phone").trim(),
                    deviceObject.optString("latitude").trim(),
                    deviceObject.optString("longitude").trim(),
                    deviceObject.optString("created_at").trim())

                if (validRecord != null) {
                    DataRepository.writeLastPoint(validRecord, alwaysWrite = false)
                }
            }
        } catch (e: Exception) {
            LogSmart.e("NetworkManager", "Exception в parseAndWriteLocations($json)", e)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun checkJsonPointRecord(phone: String?, latitude: String?, longitude: String?, dateTime: String?): PointRecord? {
        // Фунция разбирает строковые поля и возвращает в случае удачи PointRecord, иначе null.
        // (поскольку данные приходят извне, проверять надо тщательно)

        return try {
        if (phone.isNullOrBlank()) return null

        val lat = latitude?.toDoubleOrNull() ?: return null
        val lon = longitude?.toDoubleOrNull() ?: return null

        if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return null

        if (dateTime.isNullOrBlank()) return null
        val matcher = REGEXP_ISO_Z_DATE.matcher(dateTime)
        if (!matcher.matches()) return null
        Instant.parse(dateTime)
        val cleanUtcDateTime = dateTime.substring(0, 19).replace('T', ' ')

        PointRecord(phone.trim(), lat, lon, cleanUtcDateTime)

        } catch (e: Exception) {
            LogSmart.e("NetworkManager", "Exception в checkRecord( $phone, $latitude, $longitude, $dateTime)", e)
            null
        }
    }


    fun getLocationsInternet(
        onFinished: (() -> Unit)?,
        onResult: ((Result<String>) -> Unit)?
    ) {
        // Функция получает с интернет-сервера и записывает в БД координаты и заряд батареи.

        fetchJsonAsync(
            url = "https://${SettingsManager.InternetServer}/get_locations",
            token = SettingsManager.InternetToken,
            requestMethod = HttpMethod.GET,
            onFinished = onFinished,
            onResult = { result ->
                result.onSuccess { json ->
                    parseAndWriteLocations(json)
                }
                onResult?.invoke(result)
            }
        )
    }

}