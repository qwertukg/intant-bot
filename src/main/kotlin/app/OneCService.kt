package app

/**
 * Заглушка для обращения к 1С.
 * connectClient(phoneNumber, userId) — привязка телефона к userId.
 * getTicketsByTelegramId(userId) — возвращает тестовые билеты (статические).
 */
class OneCService {
    fun connectClient(phoneNumber: String, telegramUserId: Long) {
        println("OneCService.connectClient($phoneNumber, userId=$telegramUserId) [заглушка]")
        // TODO: Реализовать реальный HTTP-запрос или другую логику
    }

    fun getTicketsByTelegramId(telegramUserId: Long): List<String> {
        println("OneCService.getTicketsByTelegramId($telegramUserId) [заглушка]")
        return listOf("Билет-1234", "Билет-5678")
    }
}