package app

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*


class OneCService {
    val host = System.getenv("HOST") ?: "localhost"
    val user = System.getenv("USER") ?: "root"
    val pass = System.getenv("PASS") ?: "root"

    val client = HttpClient(CIO) {

        // TODO not used yet
        install(ContentNegotiation) {
            Json { prettyPrint = true }
        }

        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(user, pass)
                }
                sendWithoutRequest { true }
            }
        }

        HttpResponseValidator {
            validateResponse { response ->
                if (response.status != HttpStatusCode.OK) {
                    val body = response.bodyAsText()
                    val headers = response.request.headers.toString()
                    throw IllegalArgumentException("Нет подключения к 1С Вебсервису: $host\n" +
                            "> body: $body\n" +
                            "> headers: $headers\n")
                }
            }
        }
    }

    suspend fun connectClient(phoneNumber: String, telegramUserId: Long): String {

            val response = try {
                val user = Json.encodeToString(User1C( // TODO
                    phoneNumber,
                    telegramUserId
                ))
                client.post(host) {
                    contentType(ContentType.Application.Json)
                    setBody(user)
                }
            } catch (e: Throwable) {
                throw e
            }

            val body = response.bodyAsText()
            val json = Json.decodeFromString<List<Response1C>>(body) // TODO

            return json.first().Details
    }


    suspend fun getTicketsByTelegramId(telegramUserId: Long): List<String> {
            return listOf("asdf", "asdf", "asdf") // TODO
    }
}

@Serializable
data class Response1C(
    val Details: String
)

@Serializable
data class User1C(
    val phone: String,
    val identifier: Long,
)