package com.example.suas.api

import java.io.IOException
import java.util.UUID
import retrofit2.HttpException

/**
 * One logical submission owns three keys: open case, create, and submit.
 * Transport ambiguity reuses them. A completed action mints new keys.
 * MOBILE_SURFACE.md §4.4. D-022 is unrelated; this is the command key.
 */
data class SubmissionKeys(
    val openCaseKey: String,
    val createKey: String,
    val submitKey: String,
)

enum class AttemptOutcome(val reuseKey: Boolean) {
    SUCCESS(false),
    AMBIGUOUS(true),
    RETRYABLE_HTTP(true),
    DEFINITIVE_REJECTION(false),
}

class SubmissionAttempt(
    private val mint: () -> String = { UUID.randomUUID().toString() },
) {
    private var keys: SubmissionKeys? = null

    fun current(): SubmissionKeys {
        val existing = keys
        if (existing != null) return existing
        val created = SubmissionKeys(mint(), mint(), mint())
        keys = created
        return created
    }

    fun finish(outcome: AttemptOutcome) {
        if (!outcome.reuseKey) keys = null
    }
}

fun outcomeForHttp(status: Int): AttemptOutcome = when (status) {
    in 200..299 -> AttemptOutcome.SUCCESS
    401, 408, 409, 429 -> AttemptOutcome.RETRYABLE_HTTP
    in 500..599 -> AttemptOutcome.RETRYABLE_HTTP
    else -> AttemptOutcome.DEFINITIVE_REJECTION
}

fun classifyAttempt(error: Throwable): AttemptOutcome = when (error) {
    is HttpException -> outcomeForHttp(error.code())
    is IOException -> AttemptOutcome.AMBIGUOUS
    else -> AttemptOutcome.AMBIGUOUS
}

fun unauthorized(error: Throwable): Boolean =
    error is HttpException && error.code() == 401

/** 401 drops the memory bearer and still reuses the logical command key. */
fun clearSessionOnUnauthorized(status: Int, session: SessionStore): Boolean {
    if (status != 401) return false
    session.clear()
    return true
}
