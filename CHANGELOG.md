## 2025-04-21 16:14:31
- В классе `BotModule` были внесены следующие изменения:
  - В методе `addUser` была изменена переменная `ticket` на `ticketsOkResponse`.
  - Условие в переменной `successConnectionText` теперь проверяет наличие `ticketsOkResponse` и отсутствие ошибки перед выводом информации о заказе.
  - Изменена строка вывода информации о заказе, теперь используется `ticketsOkResponse.total`.
  - Добавлена переменная `ticketsAsText`.
  - Добавлена переменная `promoText`.
  - Изменена строка `text` для вывода информации о заказе, теперь также используется `ticketsOkResponse.total`.

- В классе `OneCService` были внесены следующие изменения:
  - Добавлено поле `isError` в класс `OkResponse`.
  - В методе `addUser` изменена логика установки `isError` в объекте `OkResponse`.

## 2025-04-16 12:16:11
- В классе `BotModule` были внесены изменения:
  - Изменен вывод информации о билетах в методе `sendMessageToUser`, теперь выводится описание билетов и их количество.
  - Добавлен параметр `total` в метод `sendMessageToUser`, который передается вместе с текстом сообщения.
- В классе `OneCService` были внесены изменения:
  - Добавлен параметр `total` в data class `SendTicketRequest`.
  - Добавлены комментарии к полям `total` в data class `OkResponse` и `ErrorResponse`.
- В файле `routes.kt` были внесены изменения:
  - В функции `informationRoutes` теперь принимается параметр `total` и передается в метод `sendMessageToUser`.

## 2025-04-14 12:42:05
- В методе `sendMessageToUser` класса `BotModule` теперь вместо текста принимается номер заказа `ticket`.
- В сообщении пользователю добавлен текст о успешном участии заказа в акции.

## 2025-04-11 14:36:06
- Добавлен импорт `io.ktor.server.application.log` в файле `routes.kt`.
- В функции `notificationRoutes` теперь переменная `data` заменена на `sendTicketRequest` для улучшения читаемости кода.
- Добавлено логирование информации о запросе `SendTicketRequest` в функции `notificationRoutes`.

## 2025-04-11 14:30:48
- Удален комментарий о методе отправки сообщения конкретному пользователю в классе BotModule.
- Добавлен класс SendTicketRequest с полями identifier и ticket в файле OneCService.
- В методе OneCService класса OneCService теперь при возникновении ошибки SerializationException в лог добавляется сообщение об ошибке.
- В routes.kt изменены параметры метода notificationRoutes: теперь он принимает объект SendTicketRequest, из которого извлекаются userId и ticket для отправки сообщения пользователю через метод sendMessageToUser у экземпляра класса BotModule.

## 2025-04-11 13:59:27
Увеличена версия проекта с "0.0.1" до "0.0.2".

## 2025-04-11 13:58:34
- В методе `notificationRoutes` были внесены изменения:
  - Условие добавлено для проверки `phoneNumber == null`, если так, то выполняется редирект на `"/test"`.
  - После проверки добавлен редирект на `"https://t.me/$telegramBotName?start=$phoneNumber"`.

## 2025-04-11 13:51:34
Изменено сообщение, которое отправляется пользователю при участии его заказа в акции. Теперь переменная ticketNumber?.details?.firstOrNull() подставляется без квадратных скобок.

## 2025-04-10 15:03:00
- Добавлено новое поле `promoText` в объекте `BotModule` с текстом акции и ссылкой.
- Изменён текст приглашения к использованию команды `/start` в объекте `BotModule`.
- Добавлено новое поле `successConnectionText` в объекте `BotModule` с информацией о заказе, участвующем в акции.
- Изменён текст сообщения при успешном подключении в объекте `BotModule`.
- Удален неиспользуемый код в классе `OneCService`.
- Изменён пример адреса пользователя в файле `routes.kt`.

## 2025-04-10 12:47:49
- Удалена константа HELP из объекта BotModule.
- Удалено приватное свойство botName из объекта BotModule.
- Изменена инициализация списка кнопок в методе showMainMenu объекта BotModule.
- Удален метод text(HELP) из объекта BotModule.
- Удален метод getHelp из объекта BotModule.

## 2025-04-10 09:29:51
- В классе `OneCService` были изменены названия классов и добавлен новый метод:
  - `User1C` был заменен на `AddUserRequest`
  - `Response1C` был заменен на `AddUserResponse`
  - Добавлен метод для отправки запроса на получение билетов и обработки ответа
- Добавлен новый класс `TicketsRequest` для запроса билетов.

## 2025-04-10 01:46:24
- В файле `build.gradle.kts` было добавлено подключение зависимости `io.ktor:ktor-client-auth` с версией, указанной в переменной `ktor_version`.
- Также была удалена зависимость `io.ktor:ktor-client-auth` версии 3.1.1.

## 2025-04-09 22:41:45
- Добавлен импорт `io.ktor.server.plugins.origin` в файле `routes.kt`
- Добавлен импорт `kotlinx.html.br` в файле `routes.kt`
- Заменены строки кода для получения порта, хоста и схемы запроса в методе `notificationRoutes` в файле `routes.kt`
- Изменена логика формирования `userAddressExample` и `phoneNumber` в методе `notificationRoutes` в файле `routes.kt`

## 2025-04-09 19:17:28
- Добавлен новый файл `run_bot.sh` в директорию `src/main/resources`, который содержит скрипт для запуска бота.
- В скрипте устанавливаются переменные окружения `HOST`, `USER`, `PASS`, `TELEGRAM_BOT_NAME`, `TELEGRAM_BOT_TOKEN` с значениями "asdf".
- Добавлена команда для запуска бота с помощью `java -jar bot.jar` в фоновом режиме с записью логов в файл `bot.log`.
- После запуска бота выводится сообщение о его запуске и PID процесса.

## 2025-03-29 17:38:10
- Удалён вызов метода `getTickets` с параметрами `chatId` и `userId` из объекта `BotModule`.
- Добавлен вызов метода `getTickets` с параметрами `chatId` и `userId` в объект `BotModule`.

## 2025-03-29 17:19:36
- В файле `build.gradle.kts` была изменена точка входа приложения на `app.AppKt`.
- В классе `BotModule.kt` был добавлен импорт `com.github.kotlintelegrambot.logging.LogLevel`.
- В классе `BotModule.kt` был добавлен параметр `logLevel` с уровнем логирования `All()`.
- Пакеты в файлах `BotModule.kt`, `BroadcastRequest.kt`, `OneCService.kt`, `app.kt` и `utils.kt` были изменены на `app`.
- В функции `main` в файле `app.kt` был изменен порт с 8000 на 8035.
- В функции `notificationRoutes` в файле `app.kt` был добавлен обработчик для GET запроса на `/` с ответом "BOT HOMEPAGE".
- В файле `ApplicationTest.kt` были удалены тестовые проверки.

## 2025-03-28 04:30:43
Обновлены импорты в файле `app.kt` для использования wildcard-импортов для классов из пакетов `io.ktor.http`, `io.ktor.serialization.kotlinx.json`, `io.ktor.server.application`, `io.ktor.server.engine`, `io.ktor.server.netty`, `io.ktor.server.plugins.contentnegotiation`, `io.ktor.server.request`, `io.ktor.server.response` и `io.ktor.server.routing`.

## 2025-03-28 04:30:38
- Удален импорт `com.github.kotlintelegrambot.dispatcher.Dispatcher`.
- Добавлена обработка команды `/start <номер_телефона`.
- Добавлена кнопка "Мои билеты".
- Добавлена кнопка "Справка".
