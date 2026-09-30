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
import com.example.suas.api.ActiveRequest
import com.example.suas.api.AttemptOutcome
import com.example.suas.api.Categories
import com.example.suas.api.CreateServiceRequestBody
import com.example.suas.api.ListAccess
import com.example.suas.api.RequestStatusRules
import com.example.suas.api.SuasApi
import com.example.suas.api.VeteranCommands
import com.example.suas.api.classifyAttempt
import com.example.suas.api.clearSessionOnUnauthorized
import com.example.suas.api.previewShelterName
import com.example.suas.api.unauthorized
import kotlinx.coroutines.launch
import java.util.UUID

enum class SupportKind(
    val category: String,
    val title: String,
    val disclaimer: String,
) {
    Ride(
        Categories.TRANSPORTATION,
        "Transportation",
        "Availability is confirmed by the support team. This does not book a ride.",
    ),
    Food(
        Categories.FOOD,
        "Food",
        "Ask for food support. Fulfillment is not guaranteed.",
    ),
    Shelter(
        Categories.SHELTER,
        "Temporary Shelter",
        "Ask for temporary shelter. No reservation or voucher is promised.",
    ),
    Peer(
        Categories.PEER_SUPPORT,
        "Peer Support",
        "Ask for peer or human support. Not therapy, not crisis dispatch, and not guaranteed.",
    ),
}

@Composable
fun ConnectedRequestScreen(
    kind: SupportKind,
    api: SuasApi,
    memory: RequestMemory,
    onNeedSignIn: () -> Unit,
    onBack: () -> Unit,
) {
    var pickup by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var message by remember { mutableStateOf(kind.disclaimer) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val active = memory.active?.takeIf { it.category == kind.category }

    fun fail(error: Exception, reuse: () -> Unit) {
        val outcome = classifyAttempt(error)
        reuse()
        if (unauthorized(error)) {
            clearSessionOnUnauthorized(401, memory.session)
            message = "Sign in again. This request keeps the same idempotency key."
            onNeedSignIn()
        } else if (outcome == AttemptOutcome.DEFINITIVE_REJECTION) {
            message = "The request was rejected. ${kind.disclaimer}"
        } else {
            message = "Request failed. The same request can be tried again."
        }
    }

    fun followUp(command: String, key: String, body: Map<String, String>) {
        val auth = memory.session.authorizationHeader()
        val current = memory.active
        if (auth == null || current == null) {
            onNeedSignIn()
            return
        }
        scope.launch {
            busy = true
            try {
                val updated = api.command(
                    authorization = auth,
                    idempotencyKey = key,
                    id = current.id,
                    command = command,
                    body = body,
                )
                memory.active = current.copy(status = updated.status)
                message = statusLine(updated.status, current.shelterName, kind.disclaimer)
            } catch (e: Exception) {
                if (unauthorized(e)) {
                    clearSessionOnUnauthorized(401, memory.session)
                    message = "Sign in again. This request keeps the same idempotency key."
                    onNeedSignIn()
                } else {
                    message = "Request failed. The same request can be tried again."
                }
            } finally {
                busy = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(kind.title, fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        when (kind) {
            SupportKind.Ride -> {
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
            }
            SupportKind.Food, SupportKind.Shelter -> {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Where should support go? (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            SupportKind.Peer -> {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("What kind of peer support? (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = !busy && (kind != SupportKind.Ride || (pickup.isNotBlank() && destination.isNotBlank())),
            onClick = {
                val auth = memory.session.authorizationHeader()
                if (auth == null) {
                    onNeedSignIn()
                    return@Button
                }
                scope.launch {
                    busy = true
                    val keys = memory.submitAttempt.current(payloadSignature(kind, pickup, destination, note))
                    try {
                        val opened = api.openCase(auth, keys.openCaseKey)
                        val details = when (kind) {
                            SupportKind.Ride -> mapOf(
                                "pickup_label" to pickup.trim(),
                                "destination_label" to destination.trim(),
                            )
                            SupportKind.Food, SupportKind.Shelter, SupportKind.Peer ->
                                if (note.isBlank()) emptyMap()
                                else mapOf("note" to note.trim())
                        }
                        val created = api.createServiceRequest(
                            authorization = auth,
                            idempotencyKey = keys.createKey,
                            caseId = opened.caseId,
                            body = CreateServiceRequestBody(
                                category = kind.category,
                                details = details,
                            ),
                        )
                        val submitted = api.command(
                            authorization = auth,
                            idempotencyKey = keys.submitKey,
                            id = created.serviceRequestId,
                            command = VeteranCommands.SUBMIT,
                            body = emptyMap(),
                        )
                        memory.submitAttempt.finish(AttemptOutcome.SUCCESS)
                        var shelterName: String? = null
                        var shelterDenied = false
                        if (kind == SupportKind.Shelter) {
                            try {
                                shelterName = previewShelterName(
                                    api.resources(
                                        authorization = auth,
                                        category = Categories.SHELTER,
                                        limit = ListAccess.SHELTER_PREVIEW_LIMIT,
                                    ),
                                )
                            } catch (error: Exception) {
                                shelterDenied = unauthorized(error)
                            }
                        }
                        memory.active = ActiveRequest(
                            id = submitted.serviceRequestId,
                            category = kind.category,
                            status = submitted.status,
                            confirmKey = UUID.randomUUID().toString(),
                            cancelKey = UUID.randomUUID().toString(),
                            shelterName = shelterName,
                        )
                        message = statusLine(submitted.status, shelterName, kind.disclaimer)
                        if (shelterDenied) {
                            clearSessionOnUnauthorized(401, memory.session)
                            onNeedSignIn()
                        }
                    } catch (e: Exception) {
                        fail(e) { memory.submitAttempt.finish(classifyAttempt(e)) }
                    } finally {
                        busy = false
                    }
                }
            },
        ) {
            Text("Submit request")
        }
        if (active != null && RequestStatusRules.canConfirm(active.status)) {
            Spacer(Modifier.height(8.dp))
            Button(
                enabled = !busy,
                onClick = { followUp(VeteranCommands.CONFIRM, active.confirmKey, emptyMap()) },
            ) { Text("Confirm I received this") }
        }
        if (active != null && RequestStatusRules.canCancel(active.status)) {
            Spacer(Modifier.height(8.dp))
            Button(
                enabled = !busy,
                onClick = {
                    followUp(
                        VeteranCommands.CANCEL,
                        active.cancelKey,
                        mapOf("reason" to VeteranCommands.CANCEL_REASON),
                    )
                },
            ) { Text("Cancel request") }
        }
        TextButton(onClick = onBack) { Text("Back") }
        Spacer(Modifier.height(12.dp))
        Text(message, fontSize = 14.sp)
    }
}

private fun payloadSignature(
    kind: SupportKind,
    pickup: String,
    destination: String,
    note: String,
): String = when (kind) {
    SupportKind.Ride -> listOf(kind.category, pickup.trim(), destination.trim()).joinToString("|")
    SupportKind.Food, SupportKind.Shelter, SupportKind.Peer ->
        listOf(kind.category, note.trim()).joinToString("|")
}

private fun statusLine(status: String, shelterName: String?, disclaimer: String): String {
    val headline = RequestStatusRules.headline(status)
    val shelter = if (shelterName.isNullOrBlank()) {
        ""
    } else {
        " Nearest available shelter: $shelterName. This is not a reservation."
    }
    return "$headline$shelter $disclaimer"
}
