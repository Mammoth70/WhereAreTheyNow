package ru.mammoth70.wherearetheynow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSMonitor extends BroadcastReceiver {
    // Класс слушает поток SMS. Если SMS-сообщение приходит от правильных абонентов,
    // делается парсинг сообщения (запрос геолокации или ответ с голокацией)
    // и передача дальнейшей обработки.

    @Override
    public void onReceive(Context context, Intent intent) {
        // Метод слушает входящие SMS-сообщения, парсит их и передает обработку в другие методы .
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages == null) {
                return;
            }
            String smsFrom = messages[0].getDisplayOriginatingAddress();
            if (Util.phones.contains(smsFrom)) {
                // Обработка идёт только в том случае, если телефон есть в списке.
                StringBuilder bodyText = new StringBuilder();
                for (SmsMessage message : messages) {
                    bodyText.append(message.getMessageBody());
                }
                String smsBody = bodyText.toString();
                final Pattern patternHeaderRequest = Pattern.compile(Util.HEADER_REQUEST);
                final Pattern patternHeaderRequestAnswer = Pattern.compile(Util.HEADER_REQUEST_AND_LOCATION);
                final Pattern patternHeaderAnswer = Pattern.compile(Util.HEADER_ANSWER);
                Matcher matcherHeaderRequest = patternHeaderRequest.matcher(smsBody);
                Matcher matcherHeaderRequestAnswer = patternHeaderRequestAnswer.matcher(smsBody);
                Matcher matcherHeaderAnswer = patternHeaderAnswer.matcher(smsBody);
                if (matcherHeaderRequest.find()) {
                    // Это запрос геолокации.
                    // Запрашиваем геолокацию и отвечаем.
                    requestLocation(context, smsFrom);
                } else if (matcherHeaderAnswer.find()) {
                    // Это получение геолокации.
                    // Записываем новые данные и выводим их на карту.
                    receiveLocation(context, smsFrom, smsBody, true);
                } else if (matcherHeaderRequestAnswer.find()) {
                    // Это запрос геолокации версии с координатами.
                    // Записываем новые данные, но на карту не выводим.
                    receiveLocation(context, smsFrom, smsBody, false);
                    // Запрашиваем геолокацию и отвечаем.
                    requestLocation(context, smsFrom);
                }
            }
        }
    }

    private void requestLocation(Context context, String smsTo) {
        if (Util.useService) {
            // Метод передаёт обработку запроса геолокации в GetLocationService.
            Intent intent = new Intent(context, GetLocationService.class);
            intent.putExtra(Util.INTENT_EXTRA_SMS_TO, smsTo);
            context.startService(intent);
        } else {
            // Метод передаёт обработку запроса геолокации в GetLocation.
            GetLocation getLocation = new GetLocation();
            getLocation.sendLocation(context, GetLocation.WAY_SMS, smsTo,false);
        }
    }

    private void receiveLocation(Context context, String smsFrom, String message, boolean show) {
        // Метод проверяет правильность заполнения полей SMS-сообщения с геолокацией,
        // сохраняет полученные данные в БД и в SharedPreferences
        // и передаёт обработку в MapUtil.
        Intent intent = new Intent(context, BrowserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Pattern pattern = Pattern.compile(Util.REGEXP_ANSWER);
        Matcher matcher = pattern.matcher(message);
        if ((matcher.find())) {
            PointRecord record = new PointRecord(
                smsFrom,
                Double.parseDouble(Objects.requireNonNull(matcher.group(1))),
                Double.parseDouble(Objects.requireNonNull(matcher.group(2))),
                matcher.group(3));
            MapUtil.setLastAnswer(context, record);
            if (show) {
                MapUtil.viewLocation(context, record, true);
            }

        }
    }

}