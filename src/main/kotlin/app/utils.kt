package kz.qwertukg.app

/**
 * Валидирует и возвращает Telegram token из переменных окружения.
 * Если токен отсутствует или не соответствует формату, генерирует ошибку.
 */
fun requireTelegramToken(): String {
    val tokenRegex = Regex("^[0-9]+:[A-Za-z0-9_-]{35,}$")
    val token = System.getenv("TELEGRAM_BOT_TOKEN")
        ?: throw Throwable("Telegram bot token is missing (env: TELEGRAM_BOT_TOKEN)")

    if (!tokenRegex.matches(token)) {
        throw Throwable("Invalid Telegram token format: $token")
    }
    return token
}