package com.example.suas

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.suas.api.BuildInfo
import com.example.suas.api.CrisisCopy

/**
 * Person-initiated dialer or SMS only. A direct phone-call intent is not used.
 */
@Composable
fun CrisisNotice() {
    val context = LocalContext.current
    var choosing988 by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(CrisisCopy.ENTRY_HEADING, modifier = Modifier.semantics { heading() })
        Text(CrisisCopy.IMMEDIATE_DANGER)
        Text(CrisisCopy.NOT_EMERGENCY_SERVICE)
        Text(CrisisCopy.LIFELINE)
        Text(CrisisCopy.VETERANS_CRISIS_LINE)
        Button(
            onClick = {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(CrisisCopy.CALL_911)))
            },
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(CrisisCopy.ACTION_911)
        }
        Button(
            onClick = { choosing988 = true },
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(CrisisCopy.ACTION_988)
        }
    }
    if (choosing988) {
        AlertDialog(
            onDismissRequest = { choosing988 = false },
            title = { Text(CrisisCopy.ACTION_988) },
            text = { Text(CrisisCopy.VETERANS_CRISIS_LINE) },
            confirmButton = {
                TextButton(
                    onClick = {
                        choosing988 = false
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(CrisisCopy.CALL_988)))
                    },
                ) { Text("Call 988") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        choosing988 = false
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(CrisisCopy.TEXT_988)))
                    },
                ) { Text("Text 988") }
            },
        )
    }
}

@Composable
fun BuildInfoBlock(info: BuildInfo) {
    Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text("app_version=${info.appVersion}")
        Text("commit=${info.commit}")
        Text("spec_version=${info.specVersion}")
        Text("manifest=${info.manifest}")
        Text("build_timestamp=${info.buildTimestamp}")
        Text("environment_class=${info.environmentClass}")
    }
}

@Composable
fun FailClosedScreen(reason: String, info: BuildInfo) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("This build cannot start")
        Text(reason)
        Text("No support request was sent.")
        CrisisNotice()
        BuildInfoBlock(info)
    }
}
