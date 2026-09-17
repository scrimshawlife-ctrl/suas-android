package com.example.suas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suas.api.Backend
import com.example.suas.api.ChallengeBody
import com.example.suas.api.SessionStore
import com.example.suas.api.SuasApi
import com.example.suas.api.VerifyBody
import kotlinx.coroutines.launch

/**
 * Same meaning as iOS completeLogin and web /app/join.
 * Already-enrolled only. Same on-screen line whether the address is known.
 */
@Composable
fun SignInScreen(
    api: SuasApi,
    session: SessionStore,
    onSignedIn: () -> Unit,
    onBack: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var waitingForCode by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("If this email is enrolled, a one-time sign-in code was sent.") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("Sign in", fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "Use the email on your enrollment. This does not create an account.",
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (waitingForCode) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.filter { ch -> ch.isDigit() }.take(6) },
                label = { Text("6-digit code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = !busy && email.isNotBlank() && (!waitingForCode || code.length == 6),
            onClick = {
                scope.launch {
                    busy = true
                    try {
                        if (!waitingForCode) {
                            api.issueChallenge(
                                ChallengeBody(
                                    tenantId = Backend.SYNTHETIC_TENANT_ID,
                                    destination = email.trim(),
                                ),
                            )
                            waitingForCode = true
                            message = "If this email is enrolled, a one-time sign-in code was sent."
                        } else {
                            val result = api.verify(
                                VerifyBody(
                                    tenantId = Backend.SYNTHETIC_TENANT_ID,
                                    destination = email.trim(),
                                    code = code,
                                ),
                            )
                            session.put(result.sessionCredential)
                            onSignedIn()
                        }
                    } catch (e: Exception) {
                        message = e.message ?: "Sign-in failed."
                    } finally {
                        busy = false
                    }
                }
            },
        ) {
            Text(if (waitingForCode) "Verify" else "Send sign-in code")
        }
        TextButton(onClick = onBack) { Text("Back") }
        Spacer(Modifier.height(12.dp))
        Text(message, fontSize = 14.sp)
    }
}
