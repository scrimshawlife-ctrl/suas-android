package com.example.suas.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SuasClient {
    fun create(baseUrl: String = ClientPins.API_BASE_URL): SuasApi {
        when (val decision = ClientConfiguration.validate(apiBaseUrl = baseUrl)) {
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
