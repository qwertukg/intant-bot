package app

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
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

    get("/") {
        val phoneNumber = call.queryParameters["start"]
        if (phoneNumber == null) {
            call.respondRedirect("/test")
            return@get
        }

        call.respondRedirect("https://t.me/$telegramBotName?start=$phoneNumber")
    }

    get("/test") {
        val port = call.request.port()
        val host = call.request.host()
        val scheme = call.request.origin.scheme
        val userAddressExample = "$scheme://$host:$port?start=77777737575"
        call.respondHtml {
            body {
                +"AddUser: "
                a(userAddressExample, target = "_blank") { +userAddressExample }
            }
        }
    }
}

fun Route.informationRoutes(bot: BotModule) {
    post("/send-ticket") {
        val sendTicketRequest = call.receive<SendTicketRequest>()
        val (userId, ticket) = sendTicketRequest
        bot.sendMessageToUser(userId, ticket)
        call.respondText(status = HttpStatusCode.OK, text = "Рассылка выполнена")
        application.log.info("==> SendTicketRequest: $sendTicketRequest")
    }
}