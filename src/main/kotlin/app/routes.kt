package app

import io.ktor.http.*
import io.ktor.server.html.respondHtml
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.a
import kotlinx.html.body

/**
 * Маршрут /send-broadcast (POST):
 * Принимает BroadcastRequest и рассылает сообщение
 * каждому указанному telegramUserId.
 */
fun Route.notificationRoutes(bot: BotModule) {

    // имя бота в телеграмме, узнать в @botFather
    val telegramBotName = System.getenv("TELEGRAM_BOT_NAME") ?: "@botFather"

    post("/send-broadcast") {
        val data = call.receive<BroadcastRequest>()
        val (telegramIds, message) = data
        telegramIds.forEach { tgId ->
            bot.sendMessageToUser(tgId, message)
        }
        call.respondText(status = HttpStatusCode.OK, text = "Рассылка выполнена")
    }

    //
    get("/") {
        val addressExample = "http://127.0.0.1:8035?start=87779995533"
        val phoneNumber = call.queryParameters["start"]
            ?: call.respondHtml { body { a(addressExample) { +addressExample } } }
        call.respondRedirect("https://t.me/$telegramBotName?start=$phoneNumber")

    }

}

