package com.example.suas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suas.ui.theme.BackgroundOffWhite
import com.example.suas.ui.theme.FoodGreenBg
import com.example.suas.ui.theme.FoodGreenText
import com.example.suas.ui.theme.FooterTextGrey
import com.example.suas.ui.theme.LinkGold
import com.example.suas.ui.theme.RideBlueBg
import com.example.suas.ui.theme.RideBlueText
import com.example.suas.ui.theme.ShelterPurpleBg
import com.example.suas.ui.theme.ShelterPurpleText
import com.example.suas.ui.theme.SosRed
import com.example.suas.ui.theme.SuasTheme

enum class Screen {
    Main,
    RideRequest
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuasTheme(dynamicColor = false) {
                var currentScreen by remember { mutableStateOf(Screen.Main) }

                when (currentScreen) {
                    Screen.Main -> SuasScreen(onRideClick = { currentScreen = Screen.RideRequest })
                    Screen.RideRequest -> RideRequestScreen(onBack = { currentScreen = Screen.Main })
                }
            }
        }
    }
}

@Composable
fun SuasScreen(onRideClick: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundOffWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // SOS Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SosRed)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🆘 Immediate danger? Call 988 · Press 1",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "S.U.A.S. Veteran Crisis Q.R.F.",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Instruction Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = "Tap what you need. Help is free.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Service Cards
                ServiceCard(
                    icon = "🚗",
                    title = "Free Ride",
                    subtitle = "Waymo · Amazon AV · Dispatched now",
                    backgroundColor = RideBlueBg,
                    textColor = RideBlueText,
                    onClick = onRideClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                ServiceCard(
                    icon = "🍲",
                    title = "Free Food",
                    subtitle = "Hot meal · Delivered to you",
                    backgroundColor = FoodGreenBg,
                    textColor = FoodGreenText,
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(20.dp))

                ServiceCard(
                    icon = "🏨",
                    title = "Free Shelter",
                    subtitle = "Hotel voucher · No payment needed",
                    backgroundColor = ShelterPurpleBg,
                    textColor = ShelterPurpleText,
                    onClick = {}
                )

                Spacer(modifier = Modifier.weight(1f))

                // Footer
                Column(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "All services free · Paid by corporate sponsors",
                        color = FooterTextGrey,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "suasqrf.org",
                        color = LinkGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RideRequestScreen(onBack: () -> Unit) {
    var address by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var pickupTime by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundOffWhite,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 8.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Request a Free Ride",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("My address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("My destination") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pickupTime,
                onValueChange = { pickupTime = it },
                label = { Text("Pickup time") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* Submit logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RideBlueText)
            ) {
                Text("Confirm Ride Request", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ServiceCard(
    icon: String,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, textColor, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 24.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 44.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RideRequestScreenPreview() {
    SuasTheme(dynamicColor = false) {
        RideRequestScreen(onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun SuasScreenPreview() {
    SuasTheme(dynamicColor = false) {
        SuasScreen(onRideClick = {})
    }
}
