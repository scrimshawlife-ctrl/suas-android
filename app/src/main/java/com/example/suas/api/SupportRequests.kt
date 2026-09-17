package com.example.suas.api

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class CreateServiceRequestBody(
    val category: String,
    val details: Map<String, Any> = emptyMap(),
)

data class ServiceRequestDto(
    @SerializedName("service_request_id") val serviceRequestId: String,
    @SerializedName("case_id") val caseId: String,
    val category: String,
    val status: String,
)

object Categories {
    const val TRANSPORTATION = "TRANSPORTATION"
    const val FOOD = "FOOD"
    const val SHELTER = "SHELTER"
    const val PEER_SUPPORT = "PEER_SUPPORT"
}

fun newIdempotencyKey(): String = UUID.randomUUID().toString()
