package com.example.suas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.suas.api.ActiveRequest
import com.example.suas.demo.DemoSuasApi
import com.example.suas.ui.theme.SuasTheme
import java.util.UUID

/**
 * Debug-only launcher: the real product shell over the in-memory [DemoSuasApi].
 * No server, no network, synthetic fixtures from contract/demo-fixtures.json.
 * Absent from release builds (src/debug only).
 */
class DemoRootActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuasTheme(dynamicColor = false) {
                val api = remember { DemoSuasApi() }
                val hooks = remember { demoHooks(api) }
                SuasProductShell(api = api, hooks = hooks)
            }
        }
    }
}

internal fun demoHooks(api: DemoSuasApi): ShellHooks = ShellHooks(
    environmentClass = "LOCAL",
    banner = "Demo mode: synthetic data, no server. Nothing here is a real person or provider.",
    signInHint = "Demo sign-in: ${api.primaryEmail}, code ${api.demoCode}.",
    prefillEmail = api.primaryEmail,
    existingRequest = { authorization, category ->
        runCatching { api.serviceRequestsFor(authorization) }
            .getOrDefault(emptyList())
            .lastOrNull { it.category == category }
            ?.let { request ->
                ActiveRequest(
                    id = request.serviceRequestId,
                    category = request.category,
                    status = request.status,
                    confirmKey = UUID.randomUUID().toString(),
                    cancelKey = UUID.randomUUID().toString(),
                    shelterName = null,
                )
            }
    },
    advance = { serviceRequestId -> api.demoAdvance(serviceRequestId)?.status },
)
