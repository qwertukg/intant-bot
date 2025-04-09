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

        // Инициализируем и запускаем Telegram-бот
        val bot = BotModule.apply { startBot() }

        routing {

            // POST /send-broadcast
            notificationRoutes(bot)

        }


    }.start(wait = true)
}




