package app

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Запускает Ktor-сервер на порту 8000 и Telegram-бот.
 */
fun main() {
    embeddedServer(Netty, port = 8035) {
        // Подключаем ContentNegotiation (Kotlin Serialization для JSON)
        install(ContentNegotiation) {
            json()
        }
        routing {
            // POST /send-broadcast
            notificationRoutes()
        }

        // Инициализируем и запускаем Telegram-бот
        BotModule.startBot()
    }.start(wait = true)
}

/**
 * Маршрут /send-broadcast (POST):
 * Принимает BroadcastRequest и рассылает сообщение
 * каждому указанному telegramUserId.
 */
fun Route.notificationRoutes() {
    post("/send-broadcast") {
        val data = call.receive<BroadcastRequest>()
        val (telegramIds, message) = data
        telegramIds.forEach { tgId ->
            BotModule.sendMessageToUser(tgId, message)
        }
        call.respondText(status = HttpStatusCode.OK, text = "Рассылка выполнена")
    }

    get("/") {
        call.respondText(status = HttpStatusCode.OK, text = "BOT HOMEPAGE")
    }
}


