package com.example.suas.demo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.suas.LocalRootActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Emulator acceptance for the LOCAL launcher (LocalRootActivity, src/debug only).
 *
 * Same journey as the in-memory launcher, but the client is the real /api/v0 client pointed at
 * the host Worker (npm run dev:demo in scrimshawlife-ctrl/suas) reached from the emulator at
 * http://10.0.2.2:3000.
 */
@RunWith(AndroidJUnit4::class)
class LocalLauncherSignInTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LocalRootActivity>()

    @Test
    fun localWorkerLauncherSignsInAndWalksEachSeededScreen() {
        composeRule.onNodeWithText("LOCAL Worker at http://10.0.2.2:3000", substring = true)
            .assertIsDisplayed()

        composeRule.openSignIn()
        composeRule.signInWithDemoCode(timeoutMillis = LOCAL_TIMEOUT_MS)
        composeRule.awaitSignedIn(timeoutMillis = LOCAL_TIMEOUT_MS)

        for (title in SEEDED_SCREEN_TITLES) {
            composeRule.walkSeededScreen(title)
        }

        // One real /api/v0 round trip through the host Worker. Labels only, no coordinates.
        composeRule.onNodeWithText("Transportation").performScrollTo().performClick()
        composeRule.fillRideForm(pickup = "123 Main St", destination = "VA Clinic")
        composeRule.onNodeWithText("Submit request").performClick()
        composeRule.waitUntil(timeoutMillis = LOCAL_TIMEOUT_MS) {
            composeRule.onAllNodesWithText("Cancel request").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Cancel request").assertIsDisplayed()
    }
}
