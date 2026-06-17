# Спецификация WATN API (v5.1.0)

**Описание системы:** API для первичной настройки Android-приложения и активации устройства, отправки своей геолокации и получения геолокации контактов.  
**Протокол взаимодействия:** Строго **HTTPS**.  
**Формат данных:** Все запросы и ответы используют формат **JSON**.

---

## Общие правила валидации данных

* **Токены (`token`, `api_token`):** 64 шестнадцатеричных цифры (латинские буквы от a до f в любом регистре и цифры).
* **Номера телефонов (`phone`):** Содержат только ведущий знак плюс `+` (необязателен) и цифры. Пробелы, дефисы и скобки запрещены.
* **Цвета метки (`color`):** HEX-формат - символ решетки `#` в начале и 6 шестнадцатеричных цифр (например, `#FF001A`).
* **Адрес сервера (`server`):** Возвращается в нижнем регистре. **Строго без протоколов `http://` и `https://`**. Может содержать двоеточие и цифры порта (например, `myserver.com:8443`).

---

## 1. Метод: Активация устройства

Служит для первичного связывания приложения с сервером по одноразовой ссылке.

* **Путь:** `/activate_device`
* **HTTP-метод:** `GET`
* **Параметры запроса (Query):**
  * `token` *(строка, обязательный)* - одноразовый токен активации.

### Ответы сервера:

#### HTTP 200 OK (Успех)
Успешная активация устройства.
```json
{
  "status": "success",
  "phone": "+15551234567",
  "label": "Тамазий",
  "color": "#FF001A",
  "server": "myserver.com",
  "api_token": "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789"
}
```

#### HTTP 400 Bad Request
Ошибка валидации (токен не передан или у него неверный формат).
```json
{
  "status": "error",
  "message": "Invalid or missing token"
}
```

#### HTTP 403 Forbidden
Ссылка не существует, заблокирована или просрочена.
```json
{
  "status": "error",
  "message": "Link is invalid or expired"
}
```

#### HTTP 405 Method Not Allowed
Запрос отправлен неверным HTTP-методом (ожидался только `GET`).
```json
{
  "status": "error",
  "message": "Method Not Allowed"
}
```

---

## 2. Метод: Отправка геолокации

Регулярно вызывается приложением для передачи на сервер текущих координат и состояния батареи.

* **Путь:** `/set_location`
* **HTTP-метод:** `POST`
* **Авторизация:** Требуется заголовок `Authorization: Bearer <api_token>`.
* **Тело запроса (JSON):**
```json
{
  "latitude": 57.123456,
  "longitude": 31.123456,
  "battery_level": 88
}
```

### Ответы сервера:

#### HTTP 200 OK (Успех)
Координаты успешно приняты сервером.
```json
{
  "status": "success",
  "message": "Location logged"
}
```

#### HTTP 400 Bad Request
Ошибка в теле запроса (неверный JSON или координаты вышли за допустимые границы).
```json
{
  "status": "error",
  "message": "Invalid data format"
}
// ИЛИ
{
  "status": "error",
  "message": "Coordinates out of bounds"
}
```

#### HTTP 401 Unauthorized
Ошибка авторизации (токен отсутствует или неверный формат токена).
```json
{
  "status": "error",
  "message": "Unauthorized"
}
// ИЛИ
{
  "status": "error",
  "message": "Invalid token format"
}
```

#### HTTP 403 Forbidden
Доступ запрещен (устройство отсутствует или заблокировано на сервере).
```json
{
  "status": "error",
  "message": "Forbidden"
}
```

#### HTTP 405 Method Not Allowed
Запрос отправлен неверным HTTP-методом (ожидался только `POST`).
```json
{
  "status": "error",
  "message": "Method Not Allowed"
}
```

#### HTTP 500 Internal Server Error
Внутренняя ошибка сервера.
```json
{
  "status": "error",
  "message": "Server error"
}
```

---

## 3. Метод: Получение геолокации контактов

Запрашивает у сервера последнее местоположение и статус всех связанных контактов.

* **Путь:** `/get_locations`
* **HTTP-метод:** `GET`
* **Авторизация:** Требуется заголовок `Authorization: Bearer <api_token>`.

### Ответы сервера:

#### HTTP 200 OK (Успех)
Данные успешно переданы клиенту. Поле `data` содержит список контактов или может быть пустым массивом `[]`. Координаты передаются в виде строк.
```json
{
  "status": "success",
  "data": [
    {
      "device_id": 1,
      "label": "Тамазий",
      "phone": "+15551234567",
      "color": "#FF001A",
      "latitude": 55.769842,
      "longitude": 37.633518,
      "battery_level": 88,
      "created_at": "2026-06-16T11:05:23Z"
    }
  ]
}
```

#### HTTP 401 Unauthorized
Ошибка авторизации (токен отсутствует или неверный формат токена).
```json
{
  "status": "error",
  "message": "Unauthorized"
}
// ИЛИ
{
  "status": "error",
  "message": "Invalid token format"
}
```

#### HTTP 403 Forbidden
Доступ запрещен (устройство отсутствует или заблокировано на сервере).
```json
{
  "status": "error",
  "message": "Forbidden"
}
```

#### HTTP 405 Method Not Allowed
Запрос отправлен неверным HTTP-методом (ожидался только `GET`).
```json
{
  "status": "error",
  "message": "Method Not Allowed"
}
```

#### HTTP 500 Internal Server Error
Внутренняя ошибка сервера.
```json
{
  "status": "error",
  "message": "Server error"
}
```
