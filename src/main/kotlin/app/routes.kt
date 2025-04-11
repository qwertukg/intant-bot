package app

import io.ktor.http.*
import io.ktor.server.html.respondHtml
import io.ktor.server.plugins.origin
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

    post("/send-ticket") {
        val data = call.receive<BroadcastRequest>()
        val (telegramIds, message) = data
        telegramIds.forEach { tgId ->
            bot.sendMessageToUser(tgId, message)
        }
        call.respondText(status = HttpStatusCode.OK, text = "Рассылка выполнена")
    }

    get("/") {
        val phoneNumber = call.queryParameters["start"]
        if (phoneNumber == null) {
            call.respondRedirect("/test")
            return@get
        }

        call.respondRedirect("https://t.me/$telegramBotName?start=$phoneNumber")
    }


    //
    get("/test") {
        val port = call.request.port()
        val host = call.request.host()
        val scheme = call.request.origin.scheme
        val userAddressExample = "$scheme://$host:$port?start=77777737575"
        call.respondHtml { body {
            +"AddUser: "
            a(userAddressExample, target = "_blank") { +userAddressExample }
        } }

    }

}

