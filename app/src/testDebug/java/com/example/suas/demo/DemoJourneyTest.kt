package com.example.suas.demo

import com.example.suas.api.Categories
import com.example.suas.api.ChallengeBody
import com.example.suas.api.CreateServiceRequestBody
import com.example.suas.api.VeteranCommands
import com.example.suas.api.VerifyBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

private const val TENANT_ID = "00000000-0000-4000-8000-000000000001"
private const val PRIMARY_EMAIL = "demo@example.invalid"

/**
 * Runs a suspend block to completion on the calling thread.
 *
 * The demo API never suspends (no IO, no delay), so the continuation is resumed before
 * startCoroutine returns and the value is available immediately.
 */
private fun <T> runSuspend(block: suspend () -> T): T {
    var result: Result<T>? = null
    block.startCoroutine(Continuation(EmptyCoroutineContext) { result = it })
    return result!!.getOrThrow()
}

private fun assertHttp(status: Int, block: () -> Unit) {
    val thrown: HttpException? = try {
        block()
        null
    } catch (error: HttpException) {
        error
    }
    assertNotNull("expected HttpException with status $status", thrown)
    assertEquals(status, thrown!!.code())
}

private fun createBody(category: String): CreateServiceRequestBody =
    CreateServiceRequestBody(category, mapOf("note" to "synthetic demo request"))

class DemoJourneyTest {

    private fun signIn(api: DemoSuasApi, email: String = PRIMARY_EMAIL): String {
        runSuspend { api.issueChallenge(ChallengeBody(TENANT_ID, email)) }
        val verified = runSuspend { api.verify(VerifyBody(TENANT_ID, email, api.demoCode)) }
        return "Bearer ${verified.sessionCredential}"
    }

    private fun openCaseId(api: DemoSuasApi, auth: String): String {
        val openCase = runSuspend { api.me(auth) }.openCase
        assertNotNull("signed in veteran should have an open case", openCase)
        return openCase!!.caseId
    }

    private fun createRequest(api: DemoSuasApi, auth: String, key: String, category: String): String {
        val caseId = openCaseId(api, auth)
        return runSuspend { api.createServiceRequest(auth, key, caseId, createBody(category)) }.serviceRequestId
    }

    @Test
    fun `createServiceRequest is idempotent per key and validates category and case`() {
        val api = DemoSuasApi()
        val auth = signIn(api)
        val caseId = openCaseId(api, auth)

        val first = runSuspend { api.createServiceRequest(auth, "create-1", caseId, createBody(Categories.FOOD)) }
        val replay = runSuspend { api.createServiceRequest(auth, "create-1", caseId, createBody(Categories.FOOD)) }
        assertEquals(first.serviceRequestId, replay.serviceRequestId)
        assertEquals("CREATED", first.status)

        val other = runSuspend { api.createServiceRequest(auth, "create-2", caseId, createBody(Categories.FOOD)) }
        assertNotEquals(first.serviceRequestId, other.serviceRequestId)

        assertHttp(400) {
            runSuspend { api.createServiceRequest(auth, "create-3", caseId, createBody("NOT_A_CATEGORY")) }
        }
        assertHttp(404) {
            runSuspend {
                api.createServiceRequest(
                    auth,
                    "create-4",
                    "00000000-0000-4000-8000-00000000ffff",
                    createBody(Categories.FOOD),
                )
            }
        }
    }

    @Test
    fun `the ride journey submits, requires a cancel reason, then rejects confirm`() {
        val api = DemoSuasApi()
        val auth = signIn(api)
        val rideId = createRequest(api, auth, "ride-create", Categories.TRANSPORTATION)

        val submitted = runSuspend {
            api.command(auth, "ride-submit", rideId, VeteranCommands.SUBMIT, emptyMap())
        }
        assertEquals("SUBMITTED", submitted.status)

        assertHttp(400) {
            runSuspend { api.command(auth, "ride-cancel-blank", rideId, VeteranCommands.CANCEL, emptyMap()) }
        }
        assertHttp(400) {
            runSuspend {
                api.command(
                    auth,
                    "ride-cancel-space",
                    rideId,
                    VeteranCommands.CANCEL,
                    mapOf("reason" to "   "),
                )
            }
        }

        val cancelled = runSuspend {
            api.command(
                auth,
                "ride-cancel-ok",
                rideId,
                VeteranCommands.CANCEL,
                mapOf("reason" to "synthetic change of plans"),
            )
        }
        assertEquals("CANCELLED", cancelled.status)

        assertHttp(409) {
            runSuspend { api.command(auth, "ride-confirm", rideId, VeteranCommands.CONFIRM, emptyMap()) }
        }
    }

    @Test
    fun `the food journey advances to fulfilled and confirm is idempotent`() {
        val api = DemoSuasApi()
        val auth = signIn(api)
        val foodId = createRequest(api, auth, "food-create", Categories.FOOD)

        val submitted = runSuspend {
            api.command(auth, "food-submit", foodId, VeteranCommands.SUBMIT, emptyMap())
        }
        assertEquals("SUBMITTED", submitted.status)

        var current = submitted
        var steps = 0
        while (current.status != "FULFILLED" && steps < 10) {
            val advanced = api.demoAdvance(foodId)
            assertNotNull("demoAdvance should find the request", advanced)
            current = advanced!!
            steps += 1
        }
        assertEquals("FULFILLED", current.status)

        val confirmed = runSuspend {
            api.command(auth, "food-confirm", foodId, VeteranCommands.CONFIRM, emptyMap())
        }
        assertEquals("CONFIRMED", confirmed.status)

        val replay = runSuspend {
            api.command(auth, "food-confirm", foodId, VeteranCommands.CONFIRM, emptyMap())
        }
        assertEquals("CONFIRMED", replay.status)
    }

    @Test
    fun `a non veteran command name is forbidden`() {
        val api = DemoSuasApi()
        val auth = signIn(api)
        val foodId = createRequest(api, auth, "assign-create", Categories.FOOD)

        assertHttp(403) {
            runSuspend { api.command(auth, "assign-1", foodId, "ASSIGN", emptyMap()) }
        }
    }

    @Test
    fun `resources page by category and reject unknown categories`() {
        val api = DemoSuasApi()
        val auth = signIn(api)

        val firstPage = runSuspend { api.resources(auth, Categories.FOOD, 1) }
        assertEquals(1, firstPage.resources.size)
        assertNotNull(firstPage.nextCursor)

        val all = runSuspend { api.resources(auth, Categories.FOOD, 50) }
        assertTrue(all.resources.size >= 2)
        assertNull(all.nextCursor)
        assertTrue(all.resources.all { it.serviceName?.endsWith("(synthetic)") == true })

        assertHttp(400) { runSuspend { api.resources(auth, "NOT_A_CATEGORY", 10) } }
    }

    @Test
    fun `consents include at least one active grant`() {
        val api = DemoSuasApi()
        val auth = signIn(api)

        val grants = runSuspend { api.consents(auth) }.consents
        assertTrue(grants.any { it.status == "ACTIVE" })
    }

    @Test
    fun `logout returns 204 and the bearer stops working`() {
        val api = DemoSuasApi()
        val auth = signIn(api)

        val response = runSuspend { api.logout(auth) }
        assertEquals(204, response.code())

        assertHttp(401) { runSuspend { api.me(auth) } }
    }

    @Test
    fun `serviceRequestsFor lists the four seeded categories`() {
        val api = DemoSuasApi()
        val auth = signIn(api)

        val requests = api.serviceRequestsFor(auth)
        assertEquals(4, requests.size)
        val categories = requests.map { it.category }.toSet()
        assertTrue(
            categories.containsAll(
                listOf(
                    Categories.FOOD,
                    Categories.TRANSPORTATION,
                    Categories.SHELTER,
                    Categories.PEER_SUPPORT,
                )
            )
        )
    }
}
