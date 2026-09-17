package com.example.suas.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SuasApi {
    @POST("/api/v0/auth/challenges")
    suspend fun issueChallenge(@Body body: ChallengeBody): Response<Unit>

    @POST("/api/v0/auth/challenges/commands/verify")
    suspend fun verify(@Body body: VerifyBody): VerifyResponse

    @POST("/api/v0/auth/sessions/commands/logout")
    suspend fun logout(@Header("Authorization") authorization: String): Response<Unit>

    @GET("/api/v0/veterans/me")
    suspend fun me(@Header("Authorization") authorization: String): VeteranMe

    @POST("/api/v0/cases")
    suspend fun openCase(
        @Header("Authorization") authorization: String,
        @Header("Idempotency-Key") idempotencyKey: String,
    ): OpenCaseResponse

    @POST("/api/v0/cases/{caseId}/service-requests")
    suspend fun createServiceRequest(
        @Header("Authorization") authorization: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("caseId") caseId: String,
        @Body body: CreateServiceRequestBody,
    ): ServiceRequestDto

    @POST("/api/v0/service-requests/{id}/commands/{command}")
    suspend fun command(
        @Header("Authorization") authorization: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("id") id: String,
        @Path("command") command: String,
    ): ServiceRequestDto
}
