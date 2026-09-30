package com.example.suas.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.lang.reflect.Method

class ContractTest {
    @Test
    fun localBaseNeedsAnExplicitLocalClass() {
        assertThrows(IllegalStateException::class.java) {
            SuasClient.create(baseUrl = Backend.LOCAL_BASE)
        }
        assertNotNull(SuasClient.create(baseUrl = Backend.LOCAL_BASE, environmentClass = "LOCAL"))
    }

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
        val first = attempt.current("ride|a|b")
        assertEquals("k0", first.openCaseKey)
        assertEquals("k1", first.createKey)
        assertEquals("k2", first.submitKey)
        attempt.finish(AttemptOutcome.AMBIGUOUS)
        assertEquals(first, attempt.current("ride|a|b"))
        attempt.finish(AttemptOutcome.RETRYABLE_HTTP)
        assertEquals(first, attempt.current("ride|a|b"))
        val changed = attempt.current("ride|a|c")
        assertNotEquals(first.openCaseKey, changed.openCaseKey)
        attempt.finish(AttemptOutcome.SUCCESS)
        val second = attempt.current("ride|a|c")
        assertNotEquals(changed.openCaseKey, second.openCaseKey)
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
        assertEquals(1, ListAccess.SHELTER_PREVIEW_LIMIT)
        assertEquals("PLACEHOLDER_NOT_RELEASED", ClientPins.APPLICATION_ID_STATUS)
    }

    @Test
    fun shelterPreviewUsesTheFirstNameAndDropsTheCursor() {
        val page = ResourcePage(
            resources = listOf(
                ResourceDto(serviceName = "Harbor House"),
                ResourceDto(serviceName = "Second listing"),
            ),
            limit = 1,
            nextCursor = "page-2",
        )
        assertEquals("Harbor House", previewShelterName(page))
        assertFalse(RequestStatusRules.canConfirm("SUBMITTED"))
        assertTrue(RequestStatusRules.canConfirm("FULFILLED"))
        assertTrue(RequestStatusRules.canCancel("SUBMITTED"))
        assertFalse(RequestStatusRules.canCancel("CANCELLED"))
        assertEquals(VeteranCommands.CANCEL_REASON, "Cancelled by veteran from the SUAS app.")
    }

    @Test
    fun unauthorizedClearsTheBearerAndReusesTheSubmitKey() {
        val session = SessionStore()
        session.put("tok")
        var n = 0
        val attempt = SubmissionAttempt { "k${n++}" }
        val first = attempt.current("same")
        assertTrue(clearSessionOnUnauthorized(401, session))
        attempt.finish(outcomeForHttp(401))
        assertEquals(null, session.authorizationHeader())
        assertEquals(first, attempt.current("same"))
        assertFalse(clearSessionOnUnauthorized(409, session))
    }

    @Test
    fun consentReadUsesOnlyTheLatestActiveGrant() {
        val active = ConsentGrantDto(
            permission = "can_share",
            scope = "service_request_fulfillment",
            status = "ACTIVE",
            granteeId = "provider-1",
        )
        val revoked = active.copy(status = "REVOKED")
        assertTrue(disclosureAllowed(listOf(active), "can_share", "service_request_fulfillment", "provider-1"))
        assertFalse(disclosureAllowed(listOf(revoked), "can_share", "service_request_fulfillment", "provider-1"))
        assertFalse(disclosureAllowed(emptyList(), "can_share", "service_request_fulfillment", "provider-1"))
        val firstRead = ConsentListDto(listOf(active))
        val secondRead = ConsentListDto(listOf(revoked))
        assertTrue(disclosureAllowed(firstRead, "can_share", "service_request_fulfillment", "provider-1"))
        assertFalse(disclosureAllowed(secondRead, "can_share", "service_request_fulfillment", "provider-1"))
    }

    @Test
    fun resourcesAskForOnePageAndDoNotSendACursor() {
        val resources = SuasApi::class.java.methods.first { it.name == "resources" }
        val queries = queryNames(resources)
        assertTrue(queries.contains("limit"))
        assertTrue(queries.contains("category"))
        assertFalse(queries.contains("cursor"))
        val consents = SuasApi::class.java.methods.first { it.name == "consents" }
        assertEquals("/api/v0/consents", pathOf(consents))
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

    private fun queryNames(method: Method): Set<String> {
        return method.parameterAnnotations.flatMap { annotations ->
            annotations.mapNotNull { if (it is Query) it.value else null }
        }.toSet()
    }

    private fun hasIdempotencyHeader(method: Method): Boolean {
        return method.parameterAnnotations.any { annotations ->
            annotations.any { it is Header && it.value.equals("Idempotency-Key", ignoreCase = true) }
        }
    }
}
