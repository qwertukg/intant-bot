package app

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType


class OneCService {
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
    }

    suspend fun connectClient(phoneNumber: String, telegramUserId: Long) {
        try {
            val response = client.submitForm(host, Parameters.build {
                append("phone", phoneNumber)
                append("identifier", telegramUserId.toString())
            })

//            val response = client.post(host) {
//                contentType(ContentType.Application.Json)
//                setBody(mapOf(
//                    "phone" to phoneNumber,
//                    "identifier" to telegramUserId.toString()
//                ))
//            }

            val body = response.body<String>()
            println(listOf(body, response.request.toString()))


        } catch (e: Throwable) {
            throw IllegalArgumentException("Нет подключения к 1С Вебсервису: $host", e)
        }

    }

    suspend fun getTicketsByTelegramId(telegramUserId: Long): List<String> {
        try {
            return listOf("asdf", "asdf", "asdf")
        } catch (e: Throwable) {
            throw IllegalArgumentException("Нет подключения к 1С Вебсервису: $host identifier ", e)
        }
    }
}