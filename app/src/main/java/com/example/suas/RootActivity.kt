package com.example.suas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.suas.api.BuildInfo
import com.example.suas.api.ClientConfiguration
import com.example.suas.api.ClientPins
import com.example.suas.api.ConfigurationDecision
import com.example.suas.api.SuasApi
import com.example.suas.api.SuasClient
import com.example.suas.api.signOut
import com.example.suas.ui.theme.SuasTheme
import kotlinx.coroutines.launch

private enum class RootScreen {
    Home,
    SignIn,
    Ride,
    Food,
    Shelter,
    Peer,
}

class RootActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuasTheme(dynamicColor = false) {
                when (val startup = ClientConfiguration.validate()) {
                    is ConfigurationDecision.Rejected -> FailClosedScreen(
                        startup.reason,
                        BuildInfo(
                            appVersion = ClientPins.APPLICATION_VERSION,
                            commit = ClientPins.COMMIT,
                            specVersion = ClientPins.SPEC_VERSION,
                            manifest = ClientPins.RELEASE_MANIFEST,
                            buildTimestamp = ClientPins.BUILD_TIMESTAMP,
                            environmentClass = ClientPins.ENVIRONMENT_CLASS,
                        ),
                    )
                    is ConfigurationDecision.Accepted -> SuasProductShell()
                }
            }
        }
    }
}

/**
 * The one product shell. RootActivity passes the pinned staging client and [ShellHooks.NONE].
 * Debug-only launchers pass a LOCAL client or the in-memory demo client plus demo hooks.
 */
@Composable
internal fun SuasProductShell(
    api: SuasApi = remember { SuasClient.create() },
    hooks: ShellHooks = ShellHooks.NONE,
) {
    var screen by remember { mutableStateOf(RootScreen.Home) }
    val memory = remember { RequestMemory() }
    val scope = rememberCoroutineScope()
    val sessionRejected = {
        memory.signedIn = false
        screen = RootScreen.SignIn
    }

    when (screen) {
        RootScreen.Home -> LauncherHome(
            signedIn = memory.signedIn,
            environmentClass = hooks.environmentClass,
            banner = hooks.banner,
            onSignIn = { screen = RootScreen.SignIn },
            onSignOut = {
                scope.launch {
                    signOut(api, memory.session)
                    memory.active = null
                    memory.signedIn = false
                }
            },
            onRide = { screen = RootScreen.Ride },
            onFood = { screen = RootScreen.Food },
            onShelter = { screen = RootScreen.Shelter },
            onPeer = { screen = RootScreen.Peer },
        )
        RootScreen.SignIn -> SignInScreen(
            api = api,
            session = memory.session,
            hint = hooks.signInHint,
            prefillEmail = hooks.prefillEmail,
            onSignedIn = {
                memory.signedIn = true
                screen = RootScreen.Home
            },
            onBack = { screen = RootScreen.Home },
        )
        RootScreen.Ride -> ConnectedRequestScreen(
            kind = SupportKind.Ride,
            api = api,
            memory = memory,
            hooks = hooks,
            onNeedSignIn = sessionRejected,
            onBack = { screen = RootScreen.Home },
        )
        RootScreen.Food -> ConnectedRequestScreen(
            kind = SupportKind.Food,
            api = api,
            memory = memory,
            hooks = hooks,
            onNeedSignIn = sessionRejected,
            onBack = { screen = RootScreen.Home },
        )
        RootScreen.Shelter -> ConnectedRequestScreen(
            kind = SupportKind.Shelter,
            api = api,
            memory = memory,
            hooks = hooks,
            onNeedSignIn = sessionRejected,
            onBack = { screen = RootScreen.Home },
        )
        RootScreen.Peer -> ConnectedRequestScreen(
            kind = SupportKind.Peer,
            api = api,
            memory = memory,
            hooks = hooks,
            onNeedSignIn = sessionRejected,
            onBack = { screen = RootScreen.Home },
        )
    }
}
