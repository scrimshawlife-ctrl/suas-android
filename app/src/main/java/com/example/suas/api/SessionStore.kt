package com.example.suas.api

/**
 * In-process session only. D-034 is ACCEPT_MEMORY_ONLY_DEFAULT.
 * Do not write the bearer to a preference store or a file.
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

/**
 * Drops the memory bearer before the logout call returns.
 * A failed logout does not put the bearer back.
 */
suspend fun signOut(api: SuasApi, session: SessionStore) {
    val header = session.authorizationHeader()
    session.clear()
    if (header == null) return
    try {
        api.logout(header)
    } catch (_: Exception) {
        // The bearer is already gone.
    }
}
