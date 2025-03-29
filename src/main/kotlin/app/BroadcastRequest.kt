package app

import kotlinx.serialization.Serializable

/**
 * Модель данных для массовой рассылки (POST /send-broadcast).
 * Пример JSON:
 * {
 *   "telegramIds": [12345, 67890],
 *   "message": "Привет!"
 * }
 */
@Serializable
data class BroadcastRequest(
    val telegramIds: List<Long>,
    val message: String
)