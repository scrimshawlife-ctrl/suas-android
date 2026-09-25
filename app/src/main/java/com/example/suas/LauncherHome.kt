package com.example.suas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suas.ui.theme.BackgroundOffWhite
import com.example.suas.ui.theme.FoodGreenBg
import com.example.suas.ui.theme.FoodGreenText
import com.example.suas.ui.theme.FooterTextGrey
import com.example.suas.ui.theme.LinkGold
import com.example.suas.ui.theme.PeerTealBg
import com.example.suas.ui.theme.PeerTealText
import com.example.suas.ui.theme.RideBlueBg
import com.example.suas.ui.theme.RideBlueText
import com.example.suas.ui.theme.ShelterPurpleBg
import com.example.suas.ui.theme.ShelterPurpleText

@Composable
fun LauncherHome(
    signedIn: Boolean,
    onSignIn: () -> Unit,
    onRide: () -> Unit,
    onFood: () -> Unit,
    onShelter: () -> Unit,
    onPeer: () -> Unit,
) {
    val scroll = rememberScrollState()
    Scaffold(containerColor = BackgroundOffWhite) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(scroll)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                Text(if (signedIn) "Signed in" else "Sign in")
            }
            Text(
                text = "S.U.A.S. Veteran Crisis Q.R.F.",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(28.dp))
            ServiceCard(
                icon = "🚗",
                title = "Transportation",
                subtitle = "Ask for transportation support. Availability is confirmed by the support team.",
                backgroundColor = RideBlueBg,
                textColor = RideBlueText,
                onClick = onRide,
            )
            Spacer(Modifier.height(20.dp))
            ServiceCard(
                icon = "🍲",
                title = "Food",
                subtitle = "Ask for food support. Availability and fulfillment are not guaranteed.",
                backgroundColor = FoodGreenBg,
                textColor = FoodGreenText,
                onClick = onFood,
            )
            Spacer(Modifier.height(20.dp))
            ServiceCard(
                icon = "🏨",
                title = "Temporary Shelter",
                subtitle = "Ask for temporary shelter support. No reservation or voucher is promised.",
                backgroundColor = ShelterPurpleBg,
                textColor = ShelterPurpleText,
                onClick = onShelter,
            )
            Spacer(Modifier.height(20.dp))
            ServiceCard(
                icon = "🤝",
                title = "Peer Support",
                subtitle = "Ask for peer or human support. Not therapy, not crisis dispatch, and not guaranteed.",
                backgroundColor = PeerTealBg,
                textColor = PeerTealText,
                onClick = onPeer,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "SUAS coordinates practical support. It is not an emergency service.",
                color = FooterTextGrey,
                fontSize = 15.sp,
            )
            Text(
                text = "suasqrf.org",
                color = LinkGold,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
