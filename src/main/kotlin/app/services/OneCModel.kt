package app.services

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddUserRequest(val phone: String, val identifier: Long)

@Serializable
data class SendTicketRequest(
    val identifier: Long,
    val ticket: String,
    val total: Int,
    val tickets: List<String>,
    val description: String,
)

@Serializable
data class GetTicketsRequest(val identifier: Long)

@Serializable
data class OkResponse(
    @SerialName("Details") val details: List<String>,
    @SerialName("Description") val description: String,
    @SerialName("Total") val total: Int = 0,
    var isError: Boolean = false,
)

@Serializable
data class ErrorResponse(
    @SerialName("Details") val details: String,
    @SerialName("Description") val description: String,
    @SerialName("Total") val total: Int = 0,
)