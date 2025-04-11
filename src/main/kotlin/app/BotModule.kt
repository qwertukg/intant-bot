package app

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
 * Telegram-бот.
 * Обрабатывает:
 *   1) /start <номер_телефона>
 *   2) Кнопка «Мои билеты»
 *   3) Кнопка «Справка»
 *
 * Предоставляет метод sendMessageToUser для массовой рассылки.
 */
class BotModule(val app: Application) {

    val promoText = """
Подробнее об итогах и условиях акции вы можете узнать по ссылке: https://www.instagram.com/intant_security/

Больше информации на нашем сайте: intant.kz 🌐
        """.trimIndent()

    val MY_TICKETS = "\uD83C\uDFAB Мои билеты"

    private val telegramToken = requireTelegramToken()

    // Заглушка для интеграции с 1С
    private val oneCService = OneCService(app)

    // Экземпляр бота (инициализируется в startBot)
    private lateinit var botInstance: Bot

    // Общая клавиатура: две кнопки в одном ряду
    private val replyKeyboard = KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(
                KeyboardButton(text = MY_TICKETS),
            )
        ),
        resizeKeyboard = true
    )

    /**
     * Создаёт и запускает Telegram-бота в режиме Long Polling.
     */
    fun startBot() {
        botInstance = bot {
            logLevel = LogLevel.All()
            token = telegramToken

            dispatch {
                // команда /start <номер_телефона>
                command("start") {
                    val userId = update.message?.from?.id ?: return@command
                    val chatId = update.message?.chat?.id ?: return@command
                    val phoneNumber = args.getOrNull(0)
                    handleStartCommand(userId, chatId, phoneNumber)
                }
                // кнопка «Мои билеты»
                text(MY_TICKETS) {
                    val userId = message.from?.id ?: return@text
                    val chatId = message.chat.id
                    getTickets(chatId, userId)
                }
            }
        }
        botInstance.startPolling()
    }

    /**
     * Обрабатывает /start <номер_телефона>.
     */
    private suspend fun handleStartCommand(userId: Long, chatId: Long, phoneNumber: String?) {

        if (phoneNumber == null) {
            botInstance.sendMessage(
                chatId = ChatId.Companion.fromId(chatId),
                text = promoText,
                replyMarkup = replyKeyboard
            )
            return
        }

        val ticketNumber = oneCService.addUser(phoneNumber, userId)

        val ticketsOkResponse = oneCService.getTickets(userId)
        val tickets = ticketsOkResponse?.details ?: emptyList()
        val description = ticketsOkResponse?.description

        val successConnectionText = """
🎉 Поздравляем! Ваш заказ ${ticketNumber?.details?.firstOrNull()} участвует в акции!

$MY_TICKETS: ${tickets.count()}

$description

$promoText
        """.trimIndent()

        botInstance.sendMessage(
            chatId = ChatId.Companion.fromId(chatId),
            text = successConnectionText,
            replyMarkup = replyKeyboard
        )
    }

    /**
     * Запрашивает билеты в OneCService (по userId) и отправляет в чат (chatId).
     */
    private suspend fun getTickets(chatId: Long, telegramUserId: Long) {
        val ticketsOkResponse = oneCService.getTickets(telegramUserId)
        val tickets = ticketsOkResponse?.details

        val ticketsAsText = tickets?.joinToString("\n") ?: ""

        val text = """
$ticketsAsText 

${ticketsOkResponse?.description}

$promoText
        """.trimIndent()

        botInstance.sendMessage(
            chatId = ChatId.Companion.fromId(chatId),
            text = "$MY_TICKETS: ${tickets?.count() ?: 0}\n$text",
            replyMarkup = replyKeyboard
        )
    }


    fun sendMessageToUser(telegramUserId: Long, text: String) {
        botInstance.sendMessage(
            chatId = ChatId.Companion.fromId(telegramUserId),
            text = text,
            replyMarkup = replyKeyboard
        )
    }
}