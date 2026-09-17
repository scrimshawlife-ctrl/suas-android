package com.example.suas.api

import com.google.gson.annotations.SerializedName

data class ChallengeBody(
    @SerializedName("tenant_id") val tenantId: String,
    val destination: String,
    val method: String = "EMAIL_OTP",
)

data class VerifyBody(
    @SerializedName("tenant_id") val tenantId: String,
    val destination: String,
    val code: String,
)

data class VerifyResponse(
    @SerializedName("session_credential") val sessionCredential: String,
    @SerializedName("expires_at") val expiresAt: String,
    @SerializedName("mfa_elevated") val mfaElevated: Boolean,
)

data class OpenCaseResponse(
    @SerializedName("case_id") val caseId: String,
    val status: String,
    val created: Boolean? = null,
    val replayed: Boolean? = null,
)

data class VeteranMe(
    @SerializedName("user_id") val userId: String,
    @SerializedName("open_case") val openCase: OpenCaseRef?,
)

data class OpenCaseRef(
    @SerializedName("case_id") val caseId: String,
    val status: String,
)
