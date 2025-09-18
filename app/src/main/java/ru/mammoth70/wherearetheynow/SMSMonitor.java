package ru.mammoth70.wherearetheynow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSMonitor extends BroadcastReceiver {
    // Класс слушает поток SMS. Если SMS-сообщение приходит от правильных абонентов,
    // делается парсинг сообщения (запрос геолокации или ответ с голокацией)
    // и передача дальнейшей обработки.
    private static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String BUNDLE_PDUS = "pdus";
    private static final String BUNDLE_FORMAT = "format";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Метод слушает входящие SMS-сообщения, парсит их и передает обработку в другие методы .
        if (intent != null && intent.getAction() != null && intent.getAction().equals(SMS_ACTION)) {
            Bundle bundle = intent.getExtras();
            Object[] pduArray = (Object[]) Objects.requireNonNull(bundle).get(BUNDLE_PDUS);
            if (pduArray == null) {
                return;
            }
            SmsMessage[] messages = new SmsMessage[pduArray.length];
            String format = bundle.getString(BUNDLE_FORMAT);
            for (int i = 0; i < pduArray.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i], format);
            }
            String sms_from = messages[0].getDisplayOriginatingAddress();
            if (Util.phones.contains(sms_from)) {
                // Обработка идёт только в том случае, если телефон есть в списке.
                StringBuilder bodyText = new StringBuilder();
                for (SmsMessage message : messages) {
                    bodyText.append(message.getMessageBody());
                }
                String sms_body = bodyText.toString();
                final Pattern patternHeaderRequest = Pattern.compile(Util.HEADER_REQUEST);
                final Pattern patternHeaderRequestAnswer = Pattern.compile(Util.HEADER_REQUEST_AND_LOCATION);
                final Pattern patternHeaderAnswer = Pattern.compile(Util.HEADER_ANSWER);
                Matcher matcherHeaderRequest = patternHeaderRequest.matcher(sms_body);
                Matcher matcherHeaderRequestAnswer = patternHeaderRequestAnswer.matcher(sms_body);
                Matcher matcherHeaderAnswer = patternHeaderAnswer.matcher(sms_body);
                if (matcherHeaderRequest.find()) {
                    // Это запрос геолокации.
                    // Запрашиваем геолокацию и отвечаем.
                    requestLocation(context, sms_from);
                } else if (matcherHeaderAnswer.find()) {
                    // Это получение геолокации.
                    // Записываем новые данные и выводим их на карту.
                    receiveLocation(context, sms_from, sms_body, true);
                } else if (matcherHeaderRequestAnswer.find()) {
                    // Это запрос геолокации версии с координатами.
                    // Записываем новые данные, но на карту не выводим.
                    receiveLocation(context, sms_from, sms_body, false);
                    // Запрашиваем геолокацию и отвечаем.
                    requestLocation(context, sms_from);
                }
            }
        }
    }

    private void requestLocation(Context context, String sms_to) {
        if (Util.useService) {
            // Метод передаёт обработку запроса геолокации в GetLocationService.
            Intent intent = new Intent(context, GetLocationService.class);
            intent.putExtra(Util.INTENT_EXTRA_SMS_TO, sms_to);
            context.startService(intent);
        } else {
            // Метод передаёт обработку запроса геолокации в GetLocation.
            GetLocation getLocation = new GetLocation();
            getLocation.sendLocation(context, GetLocation.WAY_SMS, sms_to,false);
        }
    }

    private void receiveLocation(Context context, String sms_from, String message, boolean show) {
        // Метод проверяет правильность заполнения полей SMS-сообщения с геолокацией,
        // сохраняет полученные данные в БД и в SharedPreferences
        // и передаёт обработку в MapUtil.
        Intent intent = new Intent(context, BrowserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Pattern pattern = Pattern.compile(Util.REGEXP_ANSWER);
        Matcher matcher = pattern.matcher(message);
        if ((matcher.find())) {
            PointRecord record = new PointRecord(
                sms_from,
                Double.parseDouble(Objects.requireNonNull(matcher.group(1))),
                Double.parseDouble(Objects.requireNonNull(matcher.group(2))),
                matcher.group(3));
            MapUtil.setLastAnswer(context, record);
            if (show) {
                MapUtil.ViewLocation(context, record, true);
            }

        }
    }

}