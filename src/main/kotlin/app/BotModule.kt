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

/**
 * Telegram-бот.
 * Обрабатывает:
 *   1) /start <номер_телефона>
 *   2) Кнопка «Мои билеты»
 *   3) Кнопка «Справка»
 *
 * Предоставляет метод sendMessageToUser для массовой рассылки.
 */
object BotModule {

    const val MY_TICKETS = "\uD83C\uDFAB Мои билеты"
    const val HELP = "❓Справка"

    private val telegramToken = requireTelegramToken()

    // Заглушка для интеграции с 1С
    private val oneCService = OneCService()

    // Экземпляр бота (инициализируется в startBot)
    private lateinit var botInstance: Bot

    // Общая клавиатура: две кнопки в одном ряду
    private val replyKeyboard = KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(KeyboardButton(text = MY_TICKETS), KeyboardButton(text = HELP))
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
                // кнопка «Справка»
                text(HELP) {
                    val chatId = message.chat.id
                    getHelp(chatId)
                }
            }
        }
        botInstance.startPolling()
    }

    /**
     * Обрабатывает /start <номер_телефона>.
     */
    private fun handleStartCommand(userId: Long, chatId: Long, phoneNumber: String?) {
        if (phoneNumber == null) {
            botInstance.sendMessage(
                chatId = ChatId.Companion.fromId(chatId),
                text = "Используйте /start <номер> (например: /start 77010001122)",
                replyMarkup = replyKeyboard
            )
            return
        }

        oneCService.connectClient(phoneNumber, userId)

        botInstance.sendMessage(
            chatId = ChatId.Companion.fromId(chatId),
            text = "Связано с номером '$phoneNumber'.\n" +
                    "Чтобы просмотреть билеты повторно, нажмите «$MY_TICKETS».",
            replyMarkup = replyKeyboard
        )

        getTickets(chatId, userId)
    }

    /**
     * Запрашивает билеты в OneCService (по userId) и отправляет в чат (chatId).
     */
    private fun getTickets(chatId: Long, telegramUserId: Long) {
        val tickets = oneCService.getTicketsByTelegramId(telegramUserId)
        if (tickets.isNotEmpty()) {
            botInstance.sendMessage(
                chatId = ChatId.Companion.fromId(chatId),
                text = "$MY_TICKETS:\n${tickets.joinToString("\n")}",
                replyMarkup = replyKeyboard
            )
        } else {
            botInstance.sendMessage(
                chatId = ChatId.Companion.fromId(chatId),
                text = "$MY_TICKETS:\nБилетов нет.",
                replyMarkup = replyKeyboard
            )
        }
    }

    /**
     * Справка по функционалу бота.
     */
    private fun getHelp(chatId: Long) {
        botInstance.sendMessage(
            chatId = ChatId.Companion.fromId(chatId),
            text = """
                $HELP
                1. /start <номер_телефона> (например: https://t.me/intant_test_bot?start=79001112233)
                   Связывает указанный номер и Telegram userId, 
                   сразу возвращает список билетов.
                
                2. $MY_TICKETS  
                   Повторный запрос билетов для текущего userId.
                
                3. Массовая рассылка  
                   POST /send-broadcast (JSON: {"telegramIds":[...], "message":"..."}).
                
                Дополнения:
                - userId — это числовой ID (не username), так как Username может отсутствовать.
            """.trimIndent(),
            replyMarkup = replyKeyboard
        )
    }

    /**
     * Публичный метод: отправка сообщения конкретному пользователю
     * (например, при массовой рассылке).
     */
    fun sendMessageToUser(telegramUserId: Long, text: String) {
        botInstance.sendMessage(
            chatId = ChatId.Companion.fromId(telegramUserId),
            text = text,
            replyMarkup = replyKeyboard
        )
    }
}