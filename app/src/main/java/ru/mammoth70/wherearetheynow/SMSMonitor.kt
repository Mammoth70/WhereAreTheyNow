package ru.mammoth70.wherearetheynow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import java.util.regex.Pattern
import ru.mammoth70.wherearetheynow.MapUtil.setLastAnswer
import ru.mammoth70.wherearetheynow.MapUtil.viewLocation
import ru.mammoth70.wherearetheynow.Util.stringToDate
import ru.mammoth70.wherearetheynow.Util.HEADER_REQUEST
import ru.mammoth70.wherearetheynow.Util.HEADER_REQUEST_AND_LOCATION
import ru.mammoth70.wherearetheynow.Util.HEADER_ANSWER
import ru.mammoth70.wherearetheynow.Util.REGEXP_ANSWER
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_SMS_TO

class SMSMonitor : BroadcastReceiver() {
    // Класс слушает поток SMS. Если SMS-сообщение приходит от правильных абонентов,
    // делается парсинг сообщения (запрос геолокации или ответ с голокацией)
    // и передача дальнейшей обработки.

    override fun onReceive(context: Context, intent: Intent) {
        // Функция слушает входящие SMS-сообщения, парсит их и передает обработку в другие функции.
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages == null) {
                return
            }
            val smsFrom = messages[0].displayOriginatingAddress
            if (smsFrom in Util.phones) {
                // Обработка идёт только в том случае, если телефон есть в списке.
                val bodyText = StringBuilder()
                for (message in messages) {
                    bodyText.append(message.messageBody)
                }
                val smsBody = bodyText.toString()
                val patternHeaderRequest = Pattern.compile(HEADER_REQUEST)
                val patternHeaderRequestAnswer = Pattern.compile(HEADER_REQUEST_AND_LOCATION)
                val patternHeaderAnswer = Pattern.compile(HEADER_ANSWER)
                val matcherHeaderRequest = patternHeaderRequest.matcher(smsBody)
                val matcherHeaderRequestAnswer = patternHeaderRequestAnswer.matcher(smsBody)
                val matcherHeaderAnswer = patternHeaderAnswer.matcher(smsBody)
                if (matcherHeaderRequest.find()) {
                    // Это запрос геолокации.
                    // Запрашиваем геолокацию и отвечаем.
                    requestLocation(context, smsFrom)
                } else if (matcherHeaderAnswer.find()) {
                    // Это получение геолокации.
                    // Записываем новые данные и выводим их на карту.
                    receiveLocation(context, smsFrom, smsBody, true)
                } else if (matcherHeaderRequestAnswer.find()) {
                    // Это запрос геолокации версии с координатами.
                    // Записываем новые данные, но на карту не выводим.
                    receiveLocation(context, smsFrom, smsBody, false)
                    // Запрашиваем геолокацию и отвечаем.
                    requestLocation(context, smsFrom)
                }
            }
        }
    }

    private fun requestLocation(context: Context, smsTo: String?) {
        if (Util.useService) {
            // Функция передаёт обработку запроса геолокации в GetLocationService.
            val intent = Intent(context, GetLocationService::class.java)
            intent.putExtra(INTENT_EXTRA_SMS_TO, smsTo)
            context.startService(intent)
        } else {
            // Функция передаёт обработку запроса геолокации в GetLocation.
            val getLocation = GetLocation()
            getLocation.sendLocation(context, GetLocation.WAY_SMS, smsTo, false)
        }
    }

    private fun receiveLocation(context: Context, smsFrom: String, message: String, show: Boolean) {
        // Функция проверяет правильность заполнения полей SMS-сообщения с геолокацией,
        // (поскольку данные приходят извне, проверять надо тщательно)
        // сохраняет полученные данные в БД и в SharedPreferences
        // и передаёт обработку в MapUtil.
        val intent = Intent(context, BrowserActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pattern = Pattern.compile(REGEXP_ANSWER)
        val matcher = pattern.matcher(message)
        if ((matcher.find())) {
            if ((matcher.group(1) != null) && (matcher.group(2) != null) && (matcher.group(3) != null)) {
                try {
                    val latitude = matcher.group(1)?.toDouble()!!
                    val longitude = matcher.group(2)?.toDouble()!!
                    val dateTime = stringToDate(matcher.group(3)!!)
                    if ((dateTime != null) &&
                        (latitude > -90) && (latitude < 90) &&
                        (longitude > -180) && (longitude < 180)
                    ) {
                        val record = PointRecord(smsFrom, latitude, longitude, dateTime)
                        setLastAnswer(context, record)
                        if (show) {
                            viewLocation(context, record, true)
                        }
                    }
                } catch (_: NumberFormatException) {
                } catch (_: NullPointerException) {
                }
            }
        }
    }

}