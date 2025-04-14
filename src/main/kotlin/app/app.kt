package app

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

/**
 * Запускает Ktor-сервер на порту 8035 и Telegram-бот.
 */
fun main() {
    val user = System.getenv("USER") ?: "root"
    val pass = System.getenv("PASS") ?: "root"

    embeddedServer(Netty, port = 8035) {


        authentication {
            basic(name = "auth-basic") {
                realm = "Ktor Server"
                validate { credentials ->
                    if (credentials.name == user && credentials.password == pass) {
                        UserIdPrincipal(credentials.name)
                    } else {
                        null
                    }
                }
            }
        }
        install(ContentNegotiation) {
            json()
        }

        // Инициализируем и запускаем Telegram-бот
        val bot = BotModule(this).apply { startBot() }

        routing {

            notificationRoutes(bot)
            authenticate("auth-basic") {
                informationRoutes(bot)
            }

        }

    }.start(wait = true)
}




