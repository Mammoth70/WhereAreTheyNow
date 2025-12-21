package ru.mammoth70.wherearetheynow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import java.util.regex.Pattern

class SMSMonitor : BroadcastReceiver() {
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
        if (smsFrom !in phones) {
            // Дальнейшая обработка идёт только в том случае, если телефон есть в списке.
            return
        }
        val bodyText = StringBuilder()
        messages.map { bodyText.append(it.messageBody) }
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
        if (useService) {
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
        val pattern = Pattern.compile(REGEXP_ANSWER)
        val matcher = pattern.matcher(message)
        if ((matcher.find())) {
            if (matcher.group(1).isNullOrBlank()) return
            if (matcher.group(2).isNullOrBlank()) return
            if (matcher.group(3).isNullOrBlank()) return
            try {
                val latitude = matcher.group(1)!!.toDouble()
                val longitude = matcher.group(2)!!.toDouble()
                val dateTime = stringToDate(matcher.group(3)!!) ?: return
                if ((latitude < -90) || (latitude > 90) || (longitude < -180) || (longitude > 180)) {
                    return
                }
                val record = PointRecord(smsFrom,latitude, longitude, dateTime)
                DBhelper.dbHelper.writeLastPoint(record)
                if (show) {
                    viewLocation(context, record, true)
                }
            } catch (_: NumberFormatException) {
            } catch (_: NullPointerException) {
            }
        }
    }

}