package com.example.suas.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SuasClient {
    fun create(
        baseUrl: String = ClientPins.API_BASE_URL,
        environmentClass: String = ClientPins.ENVIRONMENT_CLASS,
    ): SuasApi {
        when (
            val decision = ClientConfiguration.validate(
                environmentClass = environmentClass,
                apiBaseUrl = baseUrl,
            )
        ) {
            is ConfigurationDecision.Rejected ->
                error("Fail closed: ${decision.reason}")
            is ConfigurationDecision.Accepted -> Unit
        }
        val root = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(root)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SuasApi::class.java)
    }
}
