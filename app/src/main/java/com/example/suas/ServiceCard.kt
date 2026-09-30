package com.example.suas

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared launcher card. Lives in main so the release variant can compile it.
 * The debug harness calls the same composable.
 */
@Composable
fun ServiceCard(
    icon: String,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, textColor, RoundedCornerShape(20.dp))
            .clickable(
                onClick = onClick,
                role = Role.Button,
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 24.dp, horizontal = 20.dp)
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = icon,
                    fontSize = 44.sp,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}
