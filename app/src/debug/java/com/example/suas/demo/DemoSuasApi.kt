/**
 * Debug-only demo data source for the SUAS Android app.
 *
 * The fixture document `contract/demo-fixtures.json` is synthetic (fictional veterans, example.invalid
 * addresses, 555-01xx numbers) and captured from the LOCAL Worker, so every JSON shape matches
 * `/api/v0`. This file lives in the debug source set only and is never part of a release build.
 *
 * There is no server and no network: all state lives in memory for the life of the process and
 * nothing is written to disk (D-034 memory-only). It does not replace the real `/api/v0` client,
 * which stays the path for a local Worker (`http://10.0.2.2:3000`) and for staging
 * (`https://suasqrf.com`).
 */
package com.example.suas.demo

import com.example.suas.api.Categories
import com.example.suas.api.ChallengeBody
import com.example.suas.api.ConsentGrantDto
import com.example.suas.api.ConsentListDto
import com.example.suas.api.CreateServiceRequestBody
import com.example.suas.api.OpenCaseRef
import com.example.suas.api.OpenCaseResponse
import com.example.suas.api.RequestStatusRules
import com.example.suas.api.ResourceDto
import com.example.suas.api.ResourcePage
import com.example.suas.api.ServiceRequestDto
import com.example.suas.api.SuasApi
import com.example.suas.api.VeteranCommands
import com.example.suas.api.VeteranMe
import com.example.suas.api.VerifyBody
import com.example.suas.api.VerifyResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/**
 * Parsed, in-memory view of `contract/demo-fixtures.json`.
 *
 * Only the fields the demo client needs are read; unknown JSON fields are ignored so the document can
 * keep growing on the server side without breaking the debug build.
 */
class DemoFixtures private constructor(
    val demoCode: String,
    val tenantId: String,
    val veterans: List<DemoVeteran>,
    val resources: List<DemoResource>,
    val consents: List<ConsentGrantDto>,
) {

    data class DemoVeteran(
        val email: String,
        val userId: String,
        val openCase: OpenCaseRef?,
        val serviceRequests: List<ServiceRequestDto>,
    )

    data class DemoResource(
        val resourceId: String,
        val serviceName: String,
        val category: String,
    )

    companion object {
        const val RESOURCE_PATH = "/demo/demo-fixtures.json"

        fun load(): DemoFixtures {
            val stream = DemoFixtures::class.java.getResourceAsStream(RESOURCE_PATH)
                ?: throw IllegalStateException("demo fixtures missing")
            val json = stream.use { it.bufferedReader(Charsets.UTF_8).readText() }
            return parse(json)
        }

        fun parse(json: String): DemoFixtures {
            val root = JsonParser.parseString(json).asJsonObject

            val veterans = mutableListOf<DemoVeteran>()
            val enrolled = root.getAsJsonArray("enrolled")
            if (enrolled != null) {
                for (element in enrolled) {
                    val entry = element.asJsonObject
                    val me = entry.getAsJsonObject("me")

                    val serviceRequests = mutableListOf<ServiceRequestDto>()
                    val requestArray = entry.getAsJsonArray("service_requests")
                    if (requestArray != null) {
                        for (requestElement in requestArray) {
                            val request = requestElement.asJsonObject
                            serviceRequests += ServiceRequestDto(
                                serviceRequestId = request.requiredString("service_request_id"),
                                caseId = request.requiredString("case_id"),
                                category = request.requiredString("category"),
                                status = request.requiredString("status"),
                            )
                        }
                    }

                    veterans += DemoVeteran(
                        email = entry.requiredString("email"),
                        userId = me.requiredString("user_id"),
                        openCase = readOpenCase(me.get("open_case")),
                        serviceRequests = serviceRequests,
                    )
                }
            }

            val resources = mutableListOf<DemoResource>()
            val resourceArray = root.getAsJsonArray("resources")
            if (resourceArray != null) {
                for (element in resourceArray) {
                    val resource = element.asJsonObject
                    resources += DemoResource(
                        resourceId = resource.requiredString("resource_id"),
                        serviceName = resource.requiredString("service_name"),
                        category = resource.requiredString("category"),
                    )
                }
            }

            val consents = mutableListOf<ConsentGrantDto>()
            val consentArray = root.getAsJsonArray("consents")
            if (consentArray != null) {
                for (element in consentArray) {
                    val consent = element.asJsonObject
                    consents += ConsentGrantDto(
                        permission = consent.optionalString("permission"),
                        scope = consent.optionalString("scope"),
                        status = consent.optionalString("status"),
                        granteeId = consent.optionalString("grantee_id"),
                    )
                }
            }

            return DemoFixtures(
                demoCode = root.requiredString("demo_code"),
                tenantId = root.requiredString("tenant_id"),
                veterans = veterans,
                resources = resources,
                consents = consents,
            )
        }

        private fun readOpenCase(element: JsonElement?): OpenCaseRef? {
            if (element == null || element.isJsonNull) return null
            val openCase = element.asJsonObject
            return OpenCaseRef(
                caseId = openCase.requiredString("case_id"),
                status = openCase.requiredString("status"),
            )
        }

        private fun JsonObject.requiredString(name: String): String =
            get(name)?.asString ?: throw IllegalStateException("demo fixtures missing field: $name")

        private fun JsonObject.optionalString(name: String): String? {
            val element = get(name) ?: return null
            return if (element.isJsonNull) null else element.asString
        }
    }
}

/**
 * No-server implementation of [SuasApi] backed by [DemoFixtures].
 *
 * State is held in memory only and guarded by a single lock. Nothing here talks to the network, to
 * disk, or to the Android framework, so it can run under plain JUnit.
 */
class DemoSuasApi(private val fixtures: DemoFixtures = DemoFixtures.load()) : SuasApi {

    private val lock = Any()

    private val issuedChallenges = mutableSetOf<String>()
    private val sessions = mutableMapOf<String, String>()
    private val openCases = mutableMapOf<String, OpenCaseRef>()
    private val requests = linkedMapOf<String, ServiceRequestDto>()
    private val idempotency = mutableMapOf<String, Any>()
    private var counter = 0

    init {
        for (veteran in fixtures.veterans) {
            val existingCase = veteran.openCase
            if (existingCase != null) {
                openCases[veteran.userId] = existingCase
            }
            for (request in veteran.serviceRequests) {
                requests[request.serviceRequestId] = request
            }
        }
    }

    /** The synthetic verification code from the fixture document, shown by the demo harness. */
    val demoCode: String get() = fixtures.demoCode

    /** The first enrolled synthetic veteran, used to prefill the demo sign-in screen. */
    val primaryEmail: String get() = fixtures.veterans.first().email

    override suspend fun issueChallenge(body: ChallengeBody): Response<Unit> = synchronized(lock) {
        val destination = normalize(body.destination)
        if (fixtures.veterans.any { normalize(it.email) == destination }) {
            issuedChallenges += destination
        }
        Response.success(202, Unit)
    }

    override suspend fun verify(body: VerifyBody): VerifyResponse = synchronized(lock) {
        val destination = normalize(body.destination)
        val veteran = fixtures.veterans.firstOrNull { normalize(it.email) == destination }
        if (veteran == null || destination !in issuedChallenges || body.code != fixtures.demoCode) {
            throw httpError(401, "CHALLENGE_INVALID", "challenge could not be verified")
        }
        issuedChallenges -= destination
        val bearer = nextBearer()
        sessions[bearer] = veteran.userId
        VerifyResponse(
            sessionCredential = bearer,
            expiresAt = "2026-01-15T17:00:00.000Z",
            mfaElevated = false,
        )
    }

    override suspend fun logout(authorization: String): Response<Unit> = synchronized(lock) {
        if (authorization.startsWith(BEARER_PREFIX)) {
            sessions.remove(authorization.substring(BEARER_PREFIX.length).trim())
        }
        Response.success(204, Unit)
    }

    override suspend fun me(authorization: String): VeteranMe = synchronized(lock) {
        val userId = userIdFor(authorization)
        VeteranMe(userId = userId, openCase = openCases[userId])
    }

    override suspend fun openCase(authorization: String, idempotencyKey: String): OpenCaseResponse =
        synchronized(lock) {
            val userId = userIdFor(authorization)
            val replayKey = "$userId|openCase|$idempotencyKey"
            val stored = idempotency[replayKey]
            if (stored is OpenCaseResponse) return@synchronized stored.copy(replayed = true)

            val existing = openCases[userId]
            val response = if (existing != null && existing.status != "CLOSED") {
                OpenCaseResponse(
                    caseId = existing.caseId,
                    status = existing.status,
                    created = false,
                    replayed = false,
                )
            } else {
                val caseId = nextId()
                openCases[userId] = OpenCaseRef(caseId = caseId, status = "OPEN")
                OpenCaseResponse(
                    caseId = caseId,
                    status = "OPEN",
                    created = true,
                    replayed = false,
                )
            }
            idempotency[replayKey] = response
            response
        }

    override suspend fun createServiceRequest(
        authorization: String,
        idempotencyKey: String,
        caseId: String,
        body: CreateServiceRequestBody,
    ): ServiceRequestDto = synchronized(lock) {
        val userId = userIdFor(authorization)
        val replayKey = "$userId|createServiceRequest|$idempotencyKey"
        val stored = idempotency[replayKey]
        if (stored is ServiceRequestDto) return@synchronized stored

        val openCase = openCases[userId]
        if (openCase == null || openCase.caseId != caseId) {
            throw httpError(404, "NOT_FOUND", "case not found")
        }
        if (body.category !in VALID_CATEGORIES) {
            throw httpError(400, "VALIDATION_FAILED", "unknown category")
        }

        val request = ServiceRequestDto(
            serviceRequestId = nextId(),
            caseId = caseId,
            category = body.category,
            status = "CREATED",
        )
        requests[request.serviceRequestId] = request
        idempotency[replayKey] = request
        request
    }

    override suspend fun command(
        authorization: String,
        idempotencyKey: String,
        id: String,
        command: String,
        body: Map<String, String>,
    ): ServiceRequestDto = synchronized(lock) {
        val userId = userIdFor(authorization)
        val replayKey = "$userId|command|$idempotencyKey"
        val stored = idempotency[replayKey]
        if (stored is ServiceRequestDto) return@synchronized stored

        val current = requests[id] ?: throw httpError(404, "NOT_FOUND", "service request not found")
        val openCase = openCases[userId]
        if (openCase == null || current.caseId != openCase.caseId) {
            throw httpError(404, "NOT_FOUND", "service request not found")
        }

        val updated = when (command) {
            VeteranCommands.SUBMIT -> {
                if (current.status != "CREATED") {
                    throw httpError(409, "ILLEGAL_TRANSITION", "submit is not legal from ${current.status}")
                }
                current.copy(status = "SUBMITTED")
            }

            VeteranCommands.CANCEL -> {
                if (!RequestStatusRules.canCancel(current.status)) {
                    throw httpError(409, "ILLEGAL_TRANSITION", "cancel is not legal from ${current.status}")
                }
                val reason = body["reason"]
                if (reason == null || reason.isBlank()) {
                    throw httpError(400, "VALIDATION_FAILED", "cancel reason required")
                }
                current.copy(status = "CANCELLED")
            }

            VeteranCommands.CONFIRM -> {
                if (!RequestStatusRules.canConfirm(current.status)) {
                    throw httpError(409, "ILLEGAL_TRANSITION", "confirm is not legal from ${current.status}")
                }
                current.copy(status = "CONFIRMED")
            }

            else -> throw httpError(403, "FORBIDDEN", "command not permitted for this caller")
        }

        requests[id] = updated
        idempotency[replayKey] = updated
        updated
    }

    override suspend fun resources(authorization: String, category: String, limit: Int): ResourcePage =
        synchronized(lock) {
            userIdFor(authorization)
            if (category !in VALID_CATEGORIES) {
                throw httpError(400, "VALIDATION_FAILED", "unknown category")
            }
            val effectiveLimit = limit.coerceIn(1, 50)
            val matching = fixtures.resources.filter { it.category == category }
            val page = matching.take(effectiveLimit).map { resource ->
                ResourceDto(resourceId = resource.resourceId, serviceName = resource.serviceName)
            }
            ResourcePage(
                resources = page,
                limit = effectiveLimit,
                nextCursor = if (matching.size > effectiveLimit) "demo-cursor-$effectiveLimit" else null,
            )
        }

    override suspend fun consents(authorization: String): ConsentListDto = synchronized(lock) {
        userIdFor(authorization)
        ConsentListDto(consents = fixtures.consents)
    }

    /**
     * No-server stand-in for the LOCAL `POST /api/v0/dev/service-requests/{id}/simulate` helper.
     *
     * Moves one step along CREATED -> SUBMITTED -> TRIAGED -> MATCHING -> ASSIGNED -> ACCEPTED ->
     * IN_PROGRESS -> FULFILLED. Any other status is returned unchanged; an unknown id returns null.
     */
    fun demoAdvance(serviceRequestId: String): ServiceRequestDto? = synchronized(lock) {
        val current = requests[serviceRequestId] ?: return@synchronized null
        val nextStatus = ADVANCE_STEPS[current.status] ?: return@synchronized current
        val updated = current.copy(status = nextStatus)
        requests[serviceRequestId] = updated
        updated
    }

    /** Service requests on the caller's open case, in insertion order. */
    fun serviceRequestsFor(authorization: String): List<ServiceRequestDto> = synchronized(lock) {
        val userId = userIdFor(authorization)
        val openCase = openCases[userId] ?: return@synchronized emptyList()
        requests.values.filter { it.caseId == openCase.caseId }
    }

    private fun userIdFor(authorization: String): String {
        if (!authorization.startsWith(BEARER_PREFIX)) {
            throw httpError(401, "UNAUTHENTICATED", "missing session credential")
        }
        val token = authorization.substring(BEARER_PREFIX.length).trim()
        return sessions[token] ?: throw httpError(401, "UNAUTHENTICATED", "unknown session credential")
    }

    private fun nextId(): String {
        counter += 1
        return "de0000ff-0000-4000-8000-" + counter.toString().padStart(12, '0')
    }

    private fun nextBearer(): String {
        counter += 1
        return "demo-session-$counter"
    }

    private fun normalize(destination: String): String = destination.trim().lowercase()

    private fun httpError(status: Int, code: String, message: String): HttpException {
        val body = "{\"error\":{\"code\":\"$code\",\"message\":\"$message\"}}"
            .toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(status, body))
    }

    private companion object {
        const val BEARER_PREFIX = "Bearer "

        val VALID_CATEGORIES = setOf(
            Categories.TRANSPORTATION,
            Categories.FOOD,
            Categories.SHELTER,
            Categories.PEER_SUPPORT,
        )

        val ADVANCE_STEPS = mapOf(
            "CREATED" to "SUBMITTED",
            "SUBMITTED" to "TRIAGED",
            "TRIAGED" to "MATCHING",
            "MATCHING" to "ASSIGNED",
            "ASSIGNED" to "ACCEPTED",
            "ACCEPTED" to "IN_PROGRESS",
            "IN_PROGRESS" to "FULFILLED",
        )
    }
}
