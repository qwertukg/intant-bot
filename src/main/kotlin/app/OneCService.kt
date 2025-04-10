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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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

    suspend fun addUser(phoneNumber: String, telegramUserId: Long): String {
        val userRequest = Json.encodeToString(AddUserRequest(
            phoneNumber,
            telegramUserId
        ))

        // AddUser
        val response = client.post("$host/AddUser") {
            contentType(ContentType.Application.Json)
            setBody(userRequest)
        }

        val bodyAsText = response.bodyAsText()
        val json = Json.decodeFromString<List<ErrorResponse>>(bodyAsText)

        return json.first().details
    }


    suspend fun getTickets(telegramUserId: Long): List<String> {
        val ticketsRequest = Json.encodeToString(GetTicketsRequest(
            telegramUserId
        ))

        val response = client.post("$host/GetTickets") {
            contentType(ContentType.Application.Json)
            setBody(ticketsRequest)
        }

        val bodyAsText = response.bodyAsText()
        return try {
            Json.decodeFromString<List<OkResponse>>(bodyAsText).first().details
        } catch (e: SerializationException) {
            val err = Json.decodeFromString<List<ErrorResponse>>(bodyAsText)
            listOf(err.first().details)
        }
    }
}

@Serializable
data class AddUserRequest(
    val phone: String,
    val identifier: Long
)
@Serializable
data class GetTicketsRequest(
    val identifier: Long
)



@Serializable
data class OkResponse(
    @SerialName("Details")
    val details: List<String>
)
@Serializable
data class ErrorResponse(
    @SerialName("Details")
    val details: String
)