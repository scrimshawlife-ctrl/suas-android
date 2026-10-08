package com.example.suas.demo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.suas.DemoRootActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Emulator acceptance for the in-memory demo launcher (DemoRootActivity, src/debug only).
 *
 * Home, sign in with the synthetic demo code, then every seeded service screen.
 * No server and no network: the journey runs against DemoSuasApi and contract/demo-fixtures.json.
 */
@RunWith(AndroidJUnit4::class)
class DemoLauncherSignInTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<DemoRootActivity>()

    @Test
    fun inMemoryDemoLauncherSignsInAndWalksEachSeededScreen() {
        composeRule.onNodeWithText("Demo mode: synthetic data, no server", substring = true)
            .assertIsDisplayed()

        composeRule.openSignIn()
        composeRule.onNodeWithText("Demo sign-in: $DEMO_EMAIL, code $DEMO_CODE", substring = true)
            .assertIsDisplayed()
        // The hint and the prefilled field both contain the address. Match the field only.
        composeRule.onNode(hasSetTextAction().and(hasText(DEMO_EMAIL, substring = true)))
            .assertIsDisplayed()

        composeRule.signInWithDemoCode()
        composeRule.awaitSignedIn()

        for (title in SEEDED_SCREEN_TITLES) {
            composeRule.walkSeededScreen(title) {
                composeRule.awaitTextExists("Demo only: advance status")
            }
        }

        composeRule.walkSeededScreen("Transportation") {
            composeRule.onNodeWithText("Demo only: advance status", substring = true).performClick()
            composeRule.awaitTextExists("Demo only: advance status")
        }

        composeRule.onNodeWithText("Transportation", substring = false).performScrollTo()
            .assertIsDisplayed()
    }
}
