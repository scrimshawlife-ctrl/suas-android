package com.example.suas.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ApiContractTest {
    @Test
    fun prefixIsV0() {
        assertEquals("/api/v0", Backend.API_PREFIX)
    }

    @Test
    fun stagingIsHttps() {
        assertEquals("https://suasqrf.com", Backend.STAGING_BASE)
    }

    @Test
    fun sessionDoesNotInventAValue() {
        val store = SessionStore()
        assertNull(store.authorizationHeader())
        store.put("tok")
        assertEquals("Bearer tok", store.authorizationHeader())
        store.clear()
        assertNull(store.authorizationHeader())
    }

    @Test
    fun noHtmlDeployPathInContract() {
        val html = "/app/qrf/deploy"
        assertFalse(html.startsWith(Backend.API_PREFIX))
    }
}
