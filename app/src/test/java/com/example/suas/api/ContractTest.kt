package com.example.suas.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.lang.reflect.Method

class ContractTest {
    @Test
    fun shippedStagingIsAccepted() {
        val decision = ClientConfiguration.validate()
        assertTrue(decision is ConfigurationDecision.Accepted)
        val info = (decision as ConfigurationDecision.Accepted).info
        assertEquals("STAGING", info.environmentClass)
        assertEquals("0.6.0", info.specVersion)
        assertEquals("RELEASE_MANIFEST-0.6.0.md", info.manifest)
        assertEquals("1.0", info.appVersion)
        assertFalse(info.commit.isEmpty())
    }

    @Test
    fun productionWithStagingHostIsRejected() {
        val decision = ClientConfiguration.validate(environmentClass = "PRODUCTION")
        assertTrue(decision is ConfigurationDecision.Rejected)
    }

    @Test
    fun stagingWithRealEffectsIsRejected() {
        val decision = ClientConfiguration.validate(allowRealExternalEffects = true)
        assertTrue(decision is ConfigurationDecision.Rejected)
    }

    @Test
    fun wrongSpecPinIsRejected() {
        val decision = ClientConfiguration.validate(specVersion = "9.9.9")
        assertTrue(decision is ConfigurationDecision.Rejected)
    }

    @Test
    fun unknownEnvironmentIsRejected() {
        val decision = ClientConfiguration.validate(environmentClass = "PREVIEW")
        assertTrue(decision is ConfigurationDecision.Rejected)
    }

    @Test
    fun stagingHttpIsRejected() {
        val decision = ClientConfiguration.validate(apiBaseUrl = "http://suasqrf.com")
        assertTrue(decision is ConfigurationDecision.Rejected)
    }

    @Test
    fun localIsExplicitNotInferred() {
        assertTrue(
            ClientConfiguration.validate(
                environmentClass = "STAGING",
                apiBaseUrl = "http://10.0.2.2:3000",
            ) is ConfigurationDecision.Rejected,
        )
        assertTrue(
            ClientConfiguration.validate(
                environmentClass = "LOCAL",
                apiBaseUrl = "http://10.0.2.2:3000",
            ) is ConfigurationDecision.Accepted,
        )
    }

    @Test
    fun retryReusesKeysAndSuccessMintsANewAction() {
        var n = 0
        val attempt = SubmissionAttempt { "k${n++}" }
        val first = attempt.current()
        assertEquals("k0", first.openCaseKey)
        assertEquals("k1", first.createKey)
        assertEquals("k2", first.submitKey)
        attempt.finish(AttemptOutcome.AMBIGUOUS)
        assertEquals(first, attempt.current())
        attempt.finish(AttemptOutcome.RETRYABLE_HTTP)
        assertEquals(first, attempt.current())
        attempt.finish(AttemptOutcome.SUCCESS)
        val second = attempt.current()
        assertNotEquals(first.openCaseKey, second.openCaseKey)
    }

    @Test
    fun httpStatusesKeepOrDropTheKey() {
        assertEquals(AttemptOutcome.RETRYABLE_HTTP, outcomeForHttp(401))
        assertEquals(AttemptOutcome.RETRYABLE_HTTP, outcomeForHttp(409))
        assertEquals(AttemptOutcome.RETRYABLE_HTTP, outcomeForHttp(429))
        assertEquals(AttemptOutcome.RETRYABLE_HTTP, outcomeForHttp(503))
        assertEquals(AttemptOutcome.DEFINITIVE_REJECTION, outcomeForHttp(400))
        assertEquals(AttemptOutcome.SUCCESS, outcomeForHttp(200))
        assertTrue(AttemptOutcome.AMBIGUOUS.reuseKey)
        assertFalse(AttemptOutcome.SUCCESS.reuseKey)
    }

    @Test
    fun crisisDestinationsAre911And988WithoutParameters() {
        assertEquals(3, CrisisCopy.destinations.size)
        for (url in CrisisCopy.destinations) {
            assertFalse(url.contains("?"))
            assertTrue(url.endsWith("911") || url.endsWith("988"))
        }
        assertTrue(CrisisCopy.IMMEDIATE_DANGER.contains("911"))
        assertTrue(CrisisCopy.LIFELINE.contains("988"))
        assertTrue(CrisisCopy.VETERANS_CRISIS_LINE.contains("Veterans Crisis Line"))
        assertTrue(CrisisCopy.NOT_EMERGENCY_SERVICE.contains("not an emergency service"))
    }

    @Test
    fun backupIsExplicitlyOffAndHistoryIsNotWalked() {
        assertFalse(BackupPolicy.ALLOW_BACKUP)
        assertFalse(ListAccess.FOLLOWS_NEXT_CURSOR)
        assertEquals("PLACEHOLDER_NOT_RELEASED", ClientPins.APPLICATION_ID_STATUS)
    }

    @Test
    fun apiUsesV0AndUnsafeCommandsCarryIdempotency() {
        val unsafe = setOf("openCase", "createServiceRequest", "command")
        for (method in SuasApi::class.java.methods) {
            val path = pathOf(method) ?: continue
            assertTrue(path.startsWith("/api/v0/"))
            assertFalse(path.contains("/api/mobile"))
            assertFalse(path.contains("/app/"))
            assertFalse(path.contains("/dev/"))
            if (method.name in unsafe) {
                assertTrue(method.name + " missing Idempotency-Key", hasIdempotencyHeader(method))
            }
        }
    }

    private fun pathOf(method: Method): String? {
        method.getAnnotation(POST::class.java)?.let { return it.value }
        method.getAnnotation(GET::class.java)?.let { return it.value }
        return null
    }

    private fun hasIdempotencyHeader(method: Method): Boolean {
        return method.parameterAnnotations.any { annotations ->
            annotations.any { it is Header && it.value.equals("Idempotency-Key", ignoreCase = true) }
        }
    }
}
