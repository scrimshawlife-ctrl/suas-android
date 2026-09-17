package com.example.suas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.suas.api.SessionStore
import com.example.suas.api.SuasClient
import com.example.suas.ui.theme.SuasTheme

private enum class RootScreen {
    Home,
    SignIn,
    RideRequest,
}

/** Installed launcher. Instrumented tests still launch MainActivity. */
class RootActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuasTheme(dynamicColor = false) {
                var screen by remember { mutableStateOf(RootScreen.Home) }
                val session = remember { SessionStore() }
                val api = remember { SuasClient.create() }

                when (screen) {
                    RootScreen.Home -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(
                            onClick = { screen = RootScreen.SignIn },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                        ) {
                            Text(if (session.bearer == null) "Sign in" else "Signed in")
                        }
                        SuasScreen(onRideClick = { screen = RootScreen.RideRequest })
                    }
                    RootScreen.SignIn -> SignInScreen(
                        api = api,
                        session = session,
                        onSignedIn = { screen = RootScreen.Home },
                        onBack = { screen = RootScreen.Home },
                    )
                    RootScreen.RideRequest -> ConnectedRideScreen(
                        api = api,
                        session = session,
                        onNeedSignIn = { screen = RootScreen.SignIn },
                        onBack = { screen = RootScreen.Home },
                    )
                }
            }
        }
    }
}
