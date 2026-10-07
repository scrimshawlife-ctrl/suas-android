package com.example.suas.demo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput

/**
 * Shared steps for the two debug-only demo launcher journeys.
 *
 * Only text the product shell renders is used, so the steps keep working when copy changes.
 * This source set is androidTestDebug: it is compiled for the debug variant only and never
 * ships in a release build.
 */

/** Synthetic veteran seeded in suas contract/demo-fixtures.json. Not a real person. */
internal const val DEMO_EMAIL = "veteran@example.invalid"

/** Synthetic one-time code seeded in suas contract/demo-fixtures.json and in the demo Worker. */
internal const val DEMO_CODE = "246810"

/** The four seeded service screens on the home surface. */
internal val SEEDED_SCREEN_TITLES =
    listOf("Transportation", "Food", "Temporary Shelter", "Peer Support")

/** In-memory demo answers without IO, so this is generous. */
internal const val IN_MEMORY_TIMEOUT_MS = 30_000L

/** LOCAL answers over the emulator's 10.0.2.2 hop to the host Worker. */
internal const val LOCAL_TIMEOUT_MS = 90_000L

/** Home -> Sign in. The home button and the sign-in heading share the same label. */
internal fun ComposeTestRule.openSignIn() {
    waitUntil(timeoutMillis = 15_000) {
        onAllNodesWithText("Sign in").fetchSemanticsNodes().isNotEmpty()
    }
    onAllNodesWithText("Sign in")[0].performClick()
    onNodeWithText("Sign in").assertIsDisplayed()
}

/**
 * Sign-in screen -> signed-in home, using the synthetic demo email (already prefilled by both
 * launchers) and the synthetic one-time code.
 */
internal fun ComposeTestRule.signInWithDemoCode(
    code: String = DEMO_CODE,
    timeoutMillis: Long = IN_MEMORY_TIMEOUT_MS,
) {
    onNodeWithText("Send sign-in code").performClick()

    // The code field is added once the challenge call returns. The email field is already on
    // screen, so the code field is the second editable node.
    waitUntil(timeoutMillis = timeoutMillis) {
        onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size >= 2
    }
    onAllNodes(hasSetTextAction())[1].performTextInput(code)
    onNodeWithText("Verify").performClick()
}

/** Waits for the home surface to report a session, then asserts it. */
internal fun ComposeTestRule.awaitSignedIn(timeoutMillis: Long = IN_MEMORY_TIMEOUT_MS) {
    waitUntil(timeoutMillis = timeoutMillis) {
        onAllNodesWithText("Sign out").fetchSemanticsNodes().isNotEmpty()
    }
    onNodeWithText("Sign out").assertIsDisplayed()
}

/**
 * Opens one seeded service screen from the home surface, asserts the screen and its submit
 * control, runs [onScreen] while it is open, then returns home.
 */
internal fun ComposeTestRule.walkSeededScreen(title: String, onScreen: () -> Unit = {}) {
    onNodeWithText(title).performScrollTo().performClick()
    onNodeWithText(title).assertIsDisplayed()
    onNodeWithText("Submit request").assertIsDisplayed()
    onScreen()
    onNodeWithText("Back").performClick()
    onNodeWithText("Sign out").assertIsDisplayed()
}

/** Ride form labels only. No coordinates, no booking. */
internal fun ComposeTestRule.fillRideForm(pickup: String, destination: String) {
    val fields = onAllNodes(hasSetTextAction()).fetchSemanticsNodes()
    check(fields.size >= 2) { "expected pickup and destination fields, found ${fields.size}" }
    onAllNodes(hasSetTextAction())[0].performTextInput(pickup)
    onAllNodes(hasSetTextAction())[1].performTextInput(destination)
}
