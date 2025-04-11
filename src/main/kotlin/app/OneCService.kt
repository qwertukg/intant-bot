package app

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*

@Serializable
data class AddUserRequest(
    val phone: String,
    val identifier: Long
)

@Serializable
data class SendTicketRequest(
    val identifier: Long,
    val ticket: String
)

@Serializable
data class GetTicketsRequest(
    val identifier: Long
)

@Serializable
data class OkResponse(
    @SerialName("Details")
    val details: List<String>,
    @SerialName("Description")
    val description: String,

)

@Serializable
data class ErrorResponse(
    @SerialName("Details")
    val details: String,
    @SerialName("Description")
    val description: String,
)

class OneCService(val app: Application, ) {
    val host = System.getenv("HOST") ?: "localhost"
    val user = System.getenv("USER") ?: "root"
    val pass = System.getenv("PASS") ?: "root"

    val client = HttpClient(CIO) {
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(user, pass)
                }
                sendWithoutRequest { true }
            }
        }

        // если 1с НЕ отвечает 200
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status != HttpStatusCode.OK) {
                    val body = response.bodyAsText()
                    val headers = response.request.headers.toString()
                    throw AppException("==> Не OK200 от 1С: ${response}\n" +
                            "==> BODY: $body\n" +
                            "==> HEADERS: $headers\n")
                }
            }
        }
    }

    suspend fun newTicket(telegramUserId: Long, ticketNumber: String) {

    }

    suspend fun addUser(phoneNumber: String, telegramUserId: Long): OkResponse? {
        val userRequest = Json.encodeToString(AddUserRequest(
            phoneNumber,
            telegramUserId
        ))

        // call AddUser
        val response = client.post("$host/AddUser") {
            contentType(ContentType.Application.Json)
            setBody(userRequest)
        }

        return response.toOkResponse()
    }


    suspend fun getTickets(telegramUserId: Long): OkResponse? {
        val ticketsRequest = Json.encodeToString(GetTicketsRequest(
            telegramUserId
        ))

        val response = client.post("$host/GetTickets") {
            contentType(ContentType.Application.Json)
            setBody(ticketsRequest)
        }

        return response.toOkResponse()
    }

    private suspend fun HttpResponse.toOkResponse() = try {
        val okResponse = Json.decodeFromString<List<OkResponse>>(bodyAsText())
        app.log.info("==> OkResponse: $okResponse")
        okResponse.first()
    } catch (e: SerializationException) {
        try {
            val errorResponse = Json.decodeFromString<List<ErrorResponse>>(bodyAsText()).first()
            app.log.warn("==> ErrorResponse: $errorResponse")
            OkResponse(listOf(errorResponse.details), errorResponse.description)
        } catch (e: SerializationException) {
            app.log.error("==> SerializationException: ${e.message}")
            null
        }
    }


}

