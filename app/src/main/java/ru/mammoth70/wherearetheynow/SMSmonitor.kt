package ru.mammoth70.wherearetheynow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.annotation.VisibleForTesting
import java.util.regex.Pattern

private const val HEADER_REQUEST = "^WATN R$"
private const val HEADER_REQUEST_AND_LOCATION = "^WATN R "
private const val HEADER_ANSWER = "^WATN A "
private const val REGEXP_ANSWER =
    "^WATN [AR] lat (-?\\d{1,3}\\.\\d{6}), lon (-?\\d{1,3}\\.\\d{6}), time (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$"
private val pattern = Pattern.compile(REGEXP_ANSWER)

class SMSmonitor : BroadcastReceiver() {
    // Класс слушает поток SMS. Если SMS-сообщение приходит от разрешённых абонентов,
    // делается парсинг сообщения (запрос геолокации или ответ с голокацией)
    // и передача дальнейшей обработки.

    override fun onReceive(context: Context, intent: Intent) {
        // Функция слушает входящие SMS-сообщения, парсит их и передает обработку в другие функции.

        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION != intent.action) {
            return
        }
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val smsFrom = messages[0].displayOriginatingAddress
        if (DataRepository.getUser(smsFrom) == null) {
            // Дальнейшая обработка идёт только в том случае, если телефон есть в списке.
            return
        }
        val bodyText = StringBuilder()
        messages.forEach { bodyText.append(it.messageBody) }
        val smsBody = bodyText.toString()
        val patternHeaderRequest = Pattern.compile(HEADER_REQUEST)
        val patternHeaderRequestAnswer = Pattern.compile(HEADER_REQUEST_AND_LOCATION)
        val patternHeaderAnswer = Pattern.compile(HEADER_ANSWER)
        val matcherHeaderRequest = patternHeaderRequest.matcher(smsBody)
        val matcherHeaderRequestAnswer = patternHeaderRequestAnswer.matcher(smsBody)
        val matcherHeaderAnswer = patternHeaderAnswer.matcher(smsBody)
        when
            { (matcherHeaderRequest.find()) -> {
                // Это запрос геолокации без координат запросившего.
                // Определение геолокации и ответ на запрос.
                requestLocation(context, smsFrom)
            } (matcherHeaderAnswer.find()) -> {
                // Это получение геолокации.
                // Запись новых данных и вывод их на карту.
                receiveLocation(context, smsFrom, smsBody, true)
            } (matcherHeaderRequestAnswer.find()) -> {
                // Это запрос геолокации с координатами запросившего.
                // Запись новых данных, но вывод на карту не делается.
                receiveLocation(context, smsFrom, smsBody, false)
                // Определение геолокации и ответ на запрос.
                requestLocation(context, smsFrom)
            }
        }
    }


    private fun requestLocation(context: Context, smsTo: String?) {
        if (SettingsManager.useService) {
            // Функция передаёт обработку запроса геолокации в GetLocationService.
            val intent = Intent(context, GetLocationService::class.java)
            intent.putExtra(INTENT_EXTRA_SMS_TO, smsTo)
            context.startService(intent)
        } else {
            // Функция передаёт обработку запроса геолокации в GetLocation.
            val getLocation = GetLocation()
            getLocation.sendLocation(context, GetLocation.WAY_SMS,
                smsTo, false)
        }
    }


    private fun receiveLocation(context: Context, smsFrom: String, message: String, show: Boolean) {
        // Функция проверяет правильность заполнения полей SMS-сообщения с геолокацией,
        // (поскольку данные приходят извне, проверять надо тщательно)
        // сохраняет полученные данные и передаёт обработку в MapUtil.

        val record = parseSMS(smsFrom, message)
        record?.let {
            DataRepository.writeLastPoint(record)
            if (show) {
                viewLocation(context, record, true)
            }
        }
    }


    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun parseSMS(smsFrom: String, message: String): PointRecord? {
        // Фунция парсит SMS и возвращает в случае удачи PointRecord, иначе null.
        val matcher = pattern.matcher(message)
        if (!matcher.find()) return null

        return try {
            val latStr = matcher.group(1)
            val lonStr = matcher.group(2)
            val timeStr = matcher.group(3)

            val latitude = latStr?.toDouble() ?: return null
            val longitude = lonStr?.toDouble() ?: return null
            val dateTime = stringToDate(timeStr ?: "") ?: return null

            if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null

            PointRecord(smsFrom, latitude, longitude, dateTime)

        } catch (e: Exception) {
            LogSmart.e("SMSmonitor", "Ошибка парсинга SMS в parseSMS( $smsFrom , $message )", e)
            null
        }
    }

}