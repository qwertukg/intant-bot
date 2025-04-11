package app

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

/**
 * Запускает Ktor-сервер на порту 8035 и Telegram-бот.
 */
fun main() {
    embeddedServer(Netty, port = 8035) {


        // Подключаем ContentNegotiation (Kotlin Serialization для JSON)
        install(ContentNegotiation) {
            json()
        }

        // Инициализируем и запускаем Telegram-бот
        val bot = BotModule(this).apply { startBot() }

        routing {

            // POST /send-broadcast
            notificationRoutes(bot)

        }


    }.start(wait = true)
}




