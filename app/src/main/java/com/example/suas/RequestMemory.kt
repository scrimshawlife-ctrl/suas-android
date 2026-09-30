package com.example.suas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.suas.api.ActiveRequest
import com.example.suas.api.SessionStore
import com.example.suas.api.SubmissionAttempt

/**
 * Survives a trip to sign-in so a 401 does not mint a new idempotency key.
 * The bearer and any consent list are not written to disk.
 */
class RequestMemory {
    val session = SessionStore()
    val submitAttempt = SubmissionAttempt()
    var signedIn by mutableStateOf(false)
    var active by mutableStateOf<ActiveRequest?>(null)
}
