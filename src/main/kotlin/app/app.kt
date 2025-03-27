package kz.qwertukg.app

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

/**
 * Запускает Ktor-сервер на порту 8000 и Telegram-бот.
 */
fun main() {
    embeddedServer(Netty, port = 8000) {
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
}


