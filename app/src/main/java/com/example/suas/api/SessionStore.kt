package com.example.suas.api

/**
 * In-process session only (D-034 still open).
 * Do not write the bearer to SharedPreferences or a file.
 */
class SessionStore {
    @Volatile
    var bearer: String? = null
        private set

    fun put(credential: String) {
        bearer = credential
    }

    fun clear() {
        bearer = null
    }

    fun authorizationHeader(): String? =
        bearer?.let { "Bearer $it" }
}
