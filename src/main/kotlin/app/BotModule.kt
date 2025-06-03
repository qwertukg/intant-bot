package app

import app.services.OneCService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import io.ktor.server.application.Application

/**
 * Telegram-бот для работы с акцией и билетами через 1С.
 *
 * @param app Экземпляр Ktor-приложения для логирования и доступа к сервисам.
 */
class BotModule(private val app: Application) {

    private val promoText = """
Подробнее об итогах и условиях акции вы можете узнать по ссылке: https://www.instagram.com/intant_security/

Больше информации на нашем сайте: intant.kz 🌐
    """.trimIndent()

    private val MY_TICKETS = "🎫 Мои билеты"
    private val telegramToken = requireTelegramToken()
    private val oneCService = OneCService(app)
    private lateinit var botInstance: Bot

    private val replyKeyboard = KeyboardReplyMarkup(
        keyboard = listOf(listOf(KeyboardButton(text = MY_TICKETS))),
        resizeKeyboard = true
    )

    /** Запускает Telegram-бота с обработкой команд. */
    fun startBot() {
        botInstance = bot {
            logLevel = LogLevel.All()
            token = telegramToken

            dispatch {
                command("start") {
                    val userId = update.message?.from?.id ?: return@command
                    val chatId = update.message?.chat?.id ?: return@command
                    handleStartCommand(userId, chatId, args.getOrNull(0))
                }

                text(MY_TICKETS) {
                    val userId = message.from?.id ?: return@text
                    getTickets(message.chat.id, userId)
                }
            }
        }
        botInstance.startPolling()
    }

    /** Обрабатывает команду /start, связывает Telegram-пользователя с номером телефона. */
    private suspend fun handleStartCommand(userId: Long, chatId: Long, phoneNumber: String?) {
        val chat = ChatId.fromId(chatId)

        if (phoneNumber == null) {
            sendPromo(chat)
            return
        }

        val response = oneCService.addUser(phoneNumber, userId)
        val tickets = response?.details.orEmpty()
        val description = response?.description.orEmpty()
        val ticketsText = tickets.joinToString("\n")

        val message = if (response != null && !response.isError) {
            """
🎉 Поздравляем! Ваш заказ ${tickets.firstOrNull()} участвует в акции!

$description: ${response.total}

$ticketsText

$promoText
            """.trimIndent()
        } else "$ticketsText\n\n$promoText"

        botInstance.sendMessage(chat, message, replyMarkup =  replyKeyboard)
    }

    /** Отправляет пользователю список билетов, полученный из 1С. */
    private suspend fun getTickets(chatId: Long, userId: Long) {
        val responses = oneCService.getTickets(userId).orEmpty()

        val ticketsText = responses.joinToString("\n\n") { response ->
            val details = response.details.joinToString("\n")
            if (response.total != 0) "${response.description}: ${response.total}\n$details"
            else details
        }

        val fullText = "$MY_TICKETS:\n$ticketsText\n\n$promoText"
        botInstance.sendMessage(ChatId.fromId(chatId), fullText, replyMarkup =  replyKeyboard)
    }

    /** Отправляет сообщение с билетом пользователю из 1С. */
    suspend fun sendMessageToUser(userId: Long, ticket: String, total: Int, tickets: List<String>, description: String) {
        val ticketsText = tickets.joinToString("\n")
        val message = """
🎉 Поздравляем! Ваш заказ $ticket участвует в акции!

$description: $total

$ticketsText

$promoText
        """.trimIndent()

        botInstance.sendMessage(ChatId.fromId(userId), message, replyMarkup =  replyKeyboard)
    }

    /** Отправляет рекламное сообщение без регистрации. */
    private suspend fun sendPromo(chat: ChatId) {
        botInstance.sendMessage(chat, promoText, replyMarkup =  replyKeyboard)
    }
}
