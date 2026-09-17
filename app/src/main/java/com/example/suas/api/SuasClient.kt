package com.example.suas.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SuasClient {
    fun create(baseUrl: String = Backend.STAGING_BASE): SuasApi {
        val root = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(root)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SuasApi::class.java)
    }
}
