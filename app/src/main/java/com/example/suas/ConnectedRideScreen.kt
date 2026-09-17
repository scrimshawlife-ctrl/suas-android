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
import com.example.suas.api.Categories
import com.example.suas.api.CreateServiceRequestBody
import com.example.suas.api.SessionStore
import com.example.suas.api.SuasApi
import com.example.suas.api.newIdempotencyKey
import kotlinx.coroutines.launch

/**
 * Installed ride path. MainActivity still hosts the disconnected form for tests.
 * Addresses are labels only. No invented coordinates. No booking claim.
 */
@Composable
fun ConnectedRideScreen(
    api: SuasApi,
    session: SessionStore,
    onNeedSignIn: () -> Unit,
    onBack: () -> Unit,
) {
    var pickup by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Availability is confirmed by the support team. This does not book a ride.") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("Transportation", fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = pickup,
            onValueChange = { pickup = it },
            label = { Text("Pickup") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = { Text("Destination") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = !busy && pickup.isNotBlank() && destination.isNotBlank(),
            onClick = {
                val auth = session.authorizationHeader()
                if (auth == null) {
                    onNeedSignIn()
                    return@Button
                }
                scope.launch {
                    busy = true
                    try {
                        val opened = api.openCase(auth, newIdempotencyKey())
                        val created = api.createServiceRequest(
                            authorization = auth,
                            idempotencyKey = newIdempotencyKey(),
                            caseId = opened.caseId,
                            body = CreateServiceRequestBody(
                                category = Categories.TRANSPORTATION,
                                details = mapOf(
                                    "pickup_label" to pickup.trim(),
                                    "destination_label" to destination.trim(),
                                ),
                            ),
                        )
                        val submitted = api.command(
                            authorization = auth,
                            idempotencyKey = newIdempotencyKey(),
                            id = created.serviceRequestId,
                            command = "SUBMIT",
                        )
                        message = "Request ${submitted.serviceRequestId} is ${submitted.status}. Not a booked ride."
                    } catch (e: Exception) {
                        message = e.message ?: "Request failed."
                    } finally {
                        busy = false
                    }
                }
            },
        ) {
            Text("Submit request")
        }
        TextButton(onClick = onBack) { Text("Back") }
        Spacer(Modifier.height(12.dp))
        Text(message, fontSize = 14.sp)
    }
}
