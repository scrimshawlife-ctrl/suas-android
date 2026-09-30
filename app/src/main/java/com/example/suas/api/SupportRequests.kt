package com.example.suas.api

import com.google.gson.annotations.SerializedName

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

data class ResourceDto(
    @SerializedName("resource_id") val resourceId: String? = null,
    @SerializedName("service_name") val serviceName: String? = null,
)

data class ResourcePage(
    val resources: List<ResourceDto> = emptyList(),
    val limit: Int? = null,
    @SerializedName("next_cursor") val nextCursor: String? = null,
)

/** First name on the page. next_cursor is not a request and is not returned. */
fun previewShelterName(page: ResourcePage): String? {
    if (ListAccess.FOLLOWS_NEXT_CURSOR) return null
    return page.resources.firstOrNull()?.serviceName?.takeIf { it.isNotBlank() }
}

object VeteranCommands {
    const val SUBMIT = "SUBMIT"
    const val CONFIRM = "CONFIRM"
    const val CANCEL = "CANCEL"

    /** Same reason iOS sends. DISPATCH.md requires a reason on CANCEL. */
    const val CANCEL_REASON = "Cancelled by veteran from the SUAS app."
}

data class ActiveRequest(
    val id: String,
    val category: String,
    val status: String,
    val confirmKey: String,
    val cancelKey: String,
    val shelterName: String?,
)

object RequestStatusRules {
    val CANCELLABLE = setOf(
        "CREATED",
        "SUBMITTED",
        "TRIAGED",
        "MATCHING",
        "ASSIGNED",
        "ACCEPTED",
        "IN_PROGRESS",
    )

    fun canConfirm(status: String): Boolean = status == "FULFILLED"

    fun canCancel(status: String): Boolean = status in CANCELLABLE

    fun headline(status: String): String = when (status) {
        "CREATED", "SUBMITTED", "TRIAGED" -> "Request received."
        "MATCHING" -> "Looking for available support."
        "ASSIGNED" -> "A responder has the request."
        "ACCEPTED" -> "Accepted by the assigned responder."
        "IN_PROGRESS" -> "In progress."
        "FULFILLED" -> "Marked fulfilled. Confirm if you received it."
        "CONFIRMED" -> "Confirmed."
        "CLOSED" -> "This request is complete."
        "CANCELLED" -> "Request cancelled."
        "DECLINED", "EXPIRED", "UNFULFILLABLE" ->
            "No provider is available right now. The request is still recorded."
        "ESCALATED" -> "Escalated to a coordinator."
        else -> "Status: $status"
    }
}

