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

class BotModule(val app: Application) {

    val promoText = """
Подробнее об итогах и условиях акции вы можете узнать по ссылке: https://www.instagram.com/intant_security/

Больше информации на нашем сайте: intant.kz 🌐
        """.trimIndent()

    val MY_TICKETS = "\uD83C\uDFAB Мои билеты"

    private val telegramToken = requireTelegramToken()

    private val oneCService = OneCService(app)

    private lateinit var botInstance: Bot

    private val replyKeyboard = KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(
                KeyboardButton(text = MY_TICKETS),
            )
        ),
        resizeKeyboard = true
    )

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

    private suspend fun handleStartCommand(userId: Long, chatId: Long, phoneNumber: String?) {

        if (phoneNumber == null) {
            botInstance.sendMessage(
                chatId = ChatId.Companion.fromId(chatId),
                text = promoText,
                replyMarkup = replyKeyboard
            )
            return
        }

        val ticketsOkResponse = oneCService.addUser(phoneNumber, userId)

        val tickets = ticketsOkResponse?.details ?: emptyList()
        val description = ticketsOkResponse?.description
        val ticketsAsText = tickets.joinToString("\n")

        val successConnectionText = if (ticketsOkResponse != null && !ticketsOkResponse.isError) """
🎉 Поздравляем! Ваш заказ ${ticketsOkResponse.details.firstOrNull()} участвует в акции!

$description: ${ticketsOkResponse.total}

$ticketsAsText 

$promoText
        """.trimIndent() else """
$ticketsAsText 

$promoText
        """.trimIndent()

        botInstance.sendMessage(
            chatId = ChatId.Companion.fromId(chatId),
            text = successConnectionText,
            replyMarkup = replyKeyboard
        )
    }

    private suspend fun getTickets(chatId: Long, telegramUserId: Long) {
        val ticketsOkResponseList = oneCService.getTickets(telegramUserId)
        val stringBuilder = StringBuilder()
        ticketsOkResponseList?.forEach {
            val ticketsOkResponse = it
            val tickets = ticketsOkResponse.details
            val ticketsAsText = tickets.joinToString("\n")

            val text = if (ticketsOkResponse.total != 0) """
${ticketsOkResponse.description}: ${ticketsOkResponse.total}
$ticketsAsText 

            """.trimIndent() else """
$ticketsAsText 
            """.trimIndent()

            stringBuilder.appendLine(text)
        }

        stringBuilder.appendLine("""
$promoText
        """.trimIndent())


        botInstance.sendMessage(
            chatId = ChatId.Companion.fromId(chatId),
            text = "$MY_TICKETS:\n${stringBuilder}",
            replyMarkup = replyKeyboard
        )
    }

    suspend fun sendMessageToUser(telegramUserId: Long, ticket: String, total: Int, tickets: List<String>, description: String) {
        val ticketsAsText = tickets.joinToString("\n")

        val successConnectionText = """
🎉 Поздравляем! Ваш заказ $ticket участвует в акции!

$description: $total

$ticketsAsText 

$promoText
        """.trimIndent()

        botInstance.sendMessage(
            chatId = ChatId.Companion.fromId(telegramUserId),
            text = successConnectionText,
            replyMarkup = replyKeyboard
        )
    }
}