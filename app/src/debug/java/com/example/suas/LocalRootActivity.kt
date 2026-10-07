package com.example.suas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.suas.api.Backend
import com.example.suas.api.BuildInfo
import com.example.suas.api.ClientConfiguration
import com.example.suas.api.ClientPins
import com.example.suas.api.ConfigurationDecision
import com.example.suas.api.SuasClient
import com.example.suas.ui.theme.SuasTheme

/**
 * Debug-only launcher: the real product shell against a LOCAL Worker on the host
 * (`npm run dev:demo` in scrimshawlife-ctrl/suas), reached from the emulator at
 * http://10.0.2.2:3000. Environment class LOCAL is explicit, not derived from the URL.
 * Absent from release builds (src/debug only).
 */
class LocalRootActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuasTheme(dynamicColor = false) {
                when (
                    val decision = ClientConfiguration.validate(
                        environmentClass = LOCAL_CLASS,
                        apiBaseUrl = Backend.LOCAL_BASE,
                    )
                ) {
                    is ConfigurationDecision.Rejected -> FailClosedScreen(
                        decision.reason,
                        BuildInfo(
                            appVersion = ClientPins.APPLICATION_VERSION,
                            commit = ClientPins.COMMIT,
                            specVersion = ClientPins.SPEC_VERSION,
                            manifest = ClientPins.RELEASE_MANIFEST,
                            buildTimestamp = ClientPins.BUILD_TIMESTAMP,
                            environmentClass = LOCAL_CLASS,
                        ),
                    )
                    is ConfigurationDecision.Accepted -> {
                        val api = remember { SuasClient.create(Backend.LOCAL_BASE, LOCAL_CLASS) }
                        SuasProductShell(
                            api = api,
                            hooks = remember {
                                ShellHooks(
                                    environmentClass = LOCAL_CLASS,
                                    banner = "LOCAL Worker at ${Backend.LOCAL_BASE}. Synthetic demo data.",
                                    signInHint = "Demo sign-in: demo@example.invalid, code 123456 " +
                                        "(LOCAL demo Worker only).",
                                    prefillEmail = "demo@example.invalid",
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val LOCAL_CLASS = "LOCAL"
    }
}
