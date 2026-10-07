package com.example.suas.demo

import com.example.suas.api.Categories
import com.example.suas.api.ChallengeBody
import com.example.suas.api.VerifyBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/**
 * Plain JUnit 4 tests for the debug-only demo fixtures and for the sign-in surface of [DemoSuasApi].
 *
 * These run on the JVM with no Android, no Robolectric and no network. The demo API never suspends,
 * so [run] drives its suspend functions to completion on the calling thread.
 */
class DemoFixturesAndAuthTest {

    private val fixtures: DemoFixtures = DemoFixtures.load()

    private fun newApi(): DemoSuasApi = DemoSuasApi(fixtures)

    // 1. fixtures load from the classpath

    @Test
    fun fixturesLoadFromTheClasspathWithExpectedTenantCodeAndVeterans() {
        assertEquals("00000000-0000-4000-8000-000000000001", fixtures.tenantId)
        assertEquals("246810", fixtures.demoCode)
        assertEquals(2, fixtures.veterans.size)

        val emails = fixtures.veterans.map { it.email }
        assertTrue(emails.contains(PRIMARY_EMAIL))
        assertTrue(emails.contains(THIRD_EMAIL))
        for (email in emails) {
            assertTrue("unexpected veteran address $email", email.endsWith("@example.invalid"))
        }

        val categories = fixtures.resources.map { it.category }.toSet()
        for (category in ALL_CATEGORIES) {
            assertTrue("no resources seeded for category $category", categories.contains(category))
        }
    }

    // 2. the raw fixture text is synthetic: no em dash, no real-looking phone numbers

    @Test
    fun fixtureTextHasNoEmDashAndOnlyFictitiousPhoneNumbers() {
        val text = readFixtureText()
        assertFalse("fixture text contains an em dash", text.contains('\u2014'))

        val phonePattern = Regex("""\+?1?[-. ]?\(?\d{3}\)?[-. ]?\d{3}[-. ]?\d{4}""")
        // Identifiers and timestamps are digit runs, not phone numbers; drop them before the scan.
        val scannable = text
            .replace(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"), "")
            .replace(Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?Z"""), "")
        for (match in phonePattern.findAll(scannable)) {
            val start = match.range.first
            val end = match.range.last
            // UUID segments such as 00000000-0000-4000-8000-000000000001 also match the digit
            // pattern, so only standalone runs count as phone-like values.
            val insideLongerDigitRun =
                (start > 0 && scannable[start - 1].isDigit()) ||
                    (end < scannable.lastIndex && scannable[end + 1].isDigit())
            if (insideLongerDigitRun) continue

            var digits = match.value.filter { it.isDigit() }
            if (digits.length == 11) {
                digits = digits.substring(1)
            }
            assertEquals("unexpected phone-like value ${match.value}", "555", digits.substring(3, 6))
            assertEquals("unexpected phone-like value ${match.value}", "01", digits.substring(6, 8))
        }
    }

    // 3. the packaged fixture is the canonical contract document

    @Test
    fun classpathFixtureIsIdenticalToTheCanonicalContractDocument() {
        val canonical = File("../contract/demo-fixtures.json").readText(Charsets.UTF_8)
        assertEquals(
            canonical.replace("\r\n", "\n"),
            readFixtureText().replace("\r\n", "\n"),
        )
    }

    // 4. challenge issuing is intentionally indistinguishable for enrolled and unknown addresses

    @Test
    fun issueChallengeAnswers202ForEnrolledAndUnknownAddresses() {
        val api = newApi()
        val tenantId = fixtures.tenantId

        val enrolled = runSuspend {
            api.issueChallenge(ChallengeBody(tenantId = tenantId, destination = PRIMARY_EMAIL))
        }
        val unknown = runSuspend {
            api.issueChallenge(ChallengeBody(tenantId = tenantId, destination = "stranger@example.invalid"))
        }

        assertEquals(202, enrolled.code())
        assertEquals(202, unknown.code())
    }

    // 5. verification rejects wrong code, unknown address and unrequested challenge

    @Test
    fun verifyRejectsWrongCodeUnknownAddressAndUnrequestedChallenge() {
        val api = newApi()
        val tenantId = fixtures.tenantId

        assertHttp(401) {
            runSuspend {
                api.verify(VerifyBody(tenantId = tenantId, destination = "stranger@example.invalid", code = api.demoCode))
            }
        }
        assertHttp(401) {
            runSuspend {
                api.verify(VerifyBody(tenantId = tenantId, destination = PRIMARY_EMAIL, code = api.demoCode))
            }
        }

        runSuspend { api.issueChallenge(ChallengeBody(tenantId = tenantId, destination = PRIMARY_EMAIL)) }
        assertHttp(401) {
            runSuspend {
                api.verify(VerifyBody(tenantId = tenantId, destination = PRIMARY_EMAIL, code = "999999"))
            }
        }

        val verified = runSuspend {
            api.verify(VerifyBody(tenantId = tenantId, destination = PRIMARY_EMAIL, code = api.demoCode))
        }
        assertTrue(verified.sessionCredential.isNotBlank())
        assertFalse(verified.mfaElevated)
    }

    // 6. a challenge is single use

    @Test
    fun aChallengeCanOnlyBeVerifiedOnce() {
        val api = newApi()
        val tenantId = fixtures.tenantId
        runSuspend { api.issueChallenge(ChallengeBody(tenantId = tenantId, destination = PRIMARY_EMAIL)) }

        val first = runSuspend {
            api.verify(VerifyBody(tenantId = tenantId, destination = PRIMARY_EMAIL, code = api.demoCode))
        }
        assertTrue(first.sessionCredential.isNotBlank())

        assertHttp(401) {
            runSuspend {
                api.verify(VerifyBody(tenantId = tenantId, destination = PRIMARY_EMAIL, code = api.demoCode))
            }
        }
    }

    // 7. me needs a valid bearer and reports the seeded open case per veteran

    @Test
    fun meRequiresAValidBearerAndReportsTheOpenCaseOfEachVeteran() {
        val api = newApi()

        assertHttp(401) { runSuspend { api.me("Bearer demo-session-999") } }
        assertHttp(401) { runSuspend { api.me("Bearer ") } }

        val primaryMe = runSuspend { api.me(signIn(api, PRIMARY_EMAIL)) }
        assertNotNull(primaryMe.openCase)

        val thirdMe = runSuspend { api.me(signIn(api, THIRD_EMAIL)) }
        assertNull(thirdMe.openCase)
    }

    // 8. the primary veteran already has a case; the same key replays

    @Test
    fun openCaseReturnsTheExistingCaseAndReplaysTheSameKey() {
        val api = newApi()
        val auth = signIn(api, PRIMARY_EMAIL)

        val first = runSuspend { api.openCase(auth, "test-open-key-1") }
        assertEquals(false, first.created)
        assertEquals(false, first.replayed)

        val me = runSuspend { api.me(auth) }
        assertEquals(me.openCase?.caseId, first.caseId)

        val replay = runSuspend { api.openCase(auth, "test-open-key-1") }
        assertEquals(first.caseId, replay.caseId)
        assertEquals(true, replay.replayed)
    }

    // 9. a veteran without a case gets one on first use, later keys reuse it

    @Test
    fun openCaseCreatesACaseForAVeteranWithoutOneAndReusesItLater() {
        val api = newApi()
        val auth = signIn(api, THIRD_EMAIL)

        val created = runSuspend { api.openCase(auth, "test-create-key-1") }
        assertEquals(true, created.created)
        assertEquals("OPEN", created.status)

        val second = runSuspend { api.openCase(auth, "test-create-key-2") }
        assertEquals(created.caseId, second.caseId)
        assertEquals(false, second.created)
        assertEquals(false, second.replayed)
    }

    // helpers

    private fun signIn(api: DemoSuasApi, email: String = PRIMARY_EMAIL): String {
        val tenantId = fixtures.tenantId
        runSuspend { api.issueChallenge(ChallengeBody(tenantId = tenantId, destination = email)) }
        val verified = runSuspend {
            api.verify(VerifyBody(tenantId = tenantId, destination = email, code = api.demoCode))
        }
        return "Bearer ${verified.sessionCredential}"
    }

    private fun assertHttp(status: Int, block: () -> Unit) {
        try {
            block()
            fail("expected HttpException with code $status")
        } catch (expected: HttpException) {
            assertEquals(status, expected.code())
        }
    }

    private fun <T> runSuspend(block: suspend () -> T): T {
        var result: Result<T>? = null
        block.startCoroutine(Continuation(EmptyCoroutineContext) { result = it })
        return result!!.getOrThrow()
    }

    private fun readFixtureText(): String =
        DemoFixtures::class.java.getResourceAsStream(DemoFixtures.RESOURCE_PATH)!!
            .use { it.bufferedReader(Charsets.UTF_8).readText() }

    private companion object {
        const val PRIMARY_EMAIL = "veteran@example.invalid"
        const val THIRD_EMAIL = "veteran3@example.invalid"

        val ALL_CATEGORIES = listOf(
            Categories.TRANSPORTATION,
            Categories.FOOD,
            Categories.SHELTER,
            Categories.PEER_SUPPORT,
        )
    }
}
