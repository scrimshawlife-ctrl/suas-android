package com.example.suas.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Released `/api/v0` surface used by iOS `APIClient.swift`.
 * Do not add `/api/mobile`. Do not call `/app/*`.
 * Do not add `/api/v0/dev/*` — those 404 on staging.
 */
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
}
