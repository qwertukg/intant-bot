package app.services

import app.AppException
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlinx.serialization.json.*

/**
 * Сервис взаимодействия с веб-сервисом 1С.
 *
 * @param app экземпляр Ktor Application для логирования.
 */
class OneCService(private val app: Application) {

    private val host = System.getenv("HOST") ?: "http://localhost"
    private val user = System.getenv("USER") ?: "root"
    private val pass = System.getenv("PASS") ?: "root"

    private val client = HttpClient(CIO) {
        install(Auth) {
            basic {
                credentials { BasicAuthCredentials(user, pass) }
                sendWithoutRequest { true }
            }
        }

        HttpResponseValidator {
            handleResponseExceptionWithRequest { cause, request ->
                app.log.error("==> Exception during request: ${request.url}")
                app.log.error("==> Headers: ${request.headers}")
                app.log.error("==> Exception: ${cause.message}")
                throw cause
            }

            validateResponse { response ->
                val request = response.request
                val body = response.bodyAsText()

                app.log.info(
                    """${"\n"}
                    <========== 1С Request:
                    URL:        ${request.url}
                    Method:     ${request.method}
                    Headers:    ${request.headers}
                    
                    ==========> 1С Response:
                    Status:     ${response.status}
                    Headers:    ${response.headers}
                    Body:       $body
                    """.trimIndent()
                )

                if (response.status != HttpStatusCode.OK) {
                    throw AppException("Ошибка при обращении к 1С: HTTP ${response.status}")
                }
            }
        }
    }

    /**
     * Отправляет запрос на добавление пользователя в 1С.
     *
     * @param phoneNumber номер телефона.
     * @param telegramUserId идентификатор пользователя.
     * @return результат в виде [OkResponse] или null при ошибке сериализации.
     */
    suspend fun addUser(phoneNumber: String, telegramUserId: Long): OkResponse? {
        val body = client.post("$host/AddUser") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(AddUserRequest(phoneNumber, telegramUserId)))
        }.bodyAsText()
        val errResponse = Json.decodeFromString<List<ErrorResponse>>(body).first()
        return OkResponse(
            listOf(errResponse.details),
            errResponse.description,
            errResponse.total,
            true
        )
    }

    /**
     * Запрашивает список билетов у 1С.
     *
     * @param telegramUserId идентификатор пользователя.
     * @return список [OkResponse] или null при ошибке сериализации.
     */
    suspend fun getTickets(telegramUserId: Long): List<OkResponse>? {
        val body = client.post("$host/GetTickets") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(GetTicketsRequest(telegramUserId)))
        }.bodyAsText()
        return Json.decodeFromString<List<OkResponse>>(body)
    }
}

