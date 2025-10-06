# Where Are They Now?<br>Где они сейчас?

[![Android][1]][2] [![GitHub license][3]][4] [![GitHub code size in bytes][5]]()

[1]: https://img.shields.io/badge/Android-12+-blue.svg?logoColor=white&color=green
[2]: https://android.com/
[3]: https://img.shields.io/github/license/Mammoth70/WhereAreTheyNow.svg
[4]: LICENSE
[5]: https://img.shields.io/github/languages/code-size/Mammoth70/WhereAreTheyNow.svg?color=teal

Андроид-приложение для определения геолокации родственников и друзей. Обмен данными идёт с помощью SMS.  
Может подойти для тех, у кого в тарифном плане много неиспользуемых SMS.

Приложение позволяет:
- послать своему контакту SMS-запрос на определение его местоположения;
- чтобы сэкономить на количестве SMS, запрос местоположения сразу содержит в себе координаты запросившего;
- после чего, такое же приложение на стороне контакта определяет его геолокацию и отсылает SMS-ответом обратно;
- полученные координаты, в виде метки отображаются на карте;
- также, можно самостоятельно отправлять контактам SMS-сообщения с геолокацией.

Можно настроить показ геолокации на Яндекс-карте, карте OpenStreet или (в самом печальном случае) в виде списка координат.  
На Яндекс-карте показываются метки сразу нескольких контактов, можно выбрать цвет метки для каждого.
На карте OpenStreet одновременно показывается метка только одного контакта.  
В приложении поддерживается английский и руcский языки.  
Поддерживается дневная, системная и ночная темы, а также динамический цвет, сгенерированный из обоев пользователя.  
Есть настройки формата вывода карт.

## История
Подробности см. в файле [HISTORY.md](HISTORY.md).  

## Лицензирование
Данный проект распространяется по лицензии **GNU General Public License v3.0 (GPLv3)**  
Подробности см. в файле [LICENSE](LICENSE).  
Автор 2025 Андрей Яковлев <andrey-yakovlev@yandex.ru>

Иконки Android Material Icons доступны по разрешительной лицензии Apache License 2.0,  
что означает, что их можно использовать бесплатно в личных, образовательных или коммерческих проектах.

Условия использования отдельных сервисов Яндекс Карт:  
https://yandex.ru/legal/maps_api

## Licensing
This project is licensed under the **GNU General Public License v3.0 (GPLv3)**  
See the [LICENSE](LICENSE) file for details.  
Copyright 2025 Andrey Yakovlev <andrey-yakovlev@yandex.ru>

Android Material Icons are available under the permissive Apache License 2.0,  
which means they are free to use for personal, educational, or commercial projects without cost.

Terms of Use for Individual Yandex Maps Services:  
https://yandex.ru/legal/maps_api
