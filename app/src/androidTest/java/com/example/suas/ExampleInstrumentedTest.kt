package com.example.suas

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.suas", appContext.packageName)
    }

    @Test
    fun mainScreenShowsSafetyAndSupportOptions() {
        composeRule.onNodeWithText("Immediate danger or medical emergency? Call 911. Call or text 988 for the Suicide & Crisis Lifeline.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Transportation").assertIsDisplayed()
        composeRule.onNodeWithText("Food").assertIsDisplayed()
        composeRule.onNodeWithText("Temporary Shelter").assertIsDisplayed()
        composeRule.onNodeWithText("SUAS coordinates practical support. It is not an emergency service.")
            .assertIsDisplayed()
    }

    @Test
    fun transportationFormAcceptsInputButCannotSubmit() {
        composeRule.onNodeWithText("Transportation").performClick()
        composeRule.onNodeWithText("Request Transportation Support").assertIsDisplayed()

        composeRule.onNodeWithText("My address").performTextInput("123 Main St")
        composeRule.onNodeWithText("My destination").performTextInput("VA Clinic")
        composeRule.onNodeWithText("Pickup time").performTextInput("10:30 AM")

        composeRule.onNodeWithText("My address").assertTextContains("123 Main St")
        composeRule.onNodeWithText("My destination").assertTextContains("VA Clinic")
        composeRule.onNodeWithText("Pickup time").assertTextContains("10:30 AM")
        composeRule.onNodeWithText("Submission not connected").assertIsNotEnabled()
    }

    @Test
    fun transportationBackReturnsToMainScreen() {
        composeRule.onNodeWithText("Transportation").performClick()
        composeRule.onNodeWithContentDescription("Back").assertIsEnabled().performClick()
        composeRule.onNodeWithText("S.U.A.S. Veteran Support").assertIsDisplayed()
    }

    @Test
    fun unimplementedSupportCardsRemainOnMainScreen() {
        composeRule.onNodeWithText("Food").performClick()
        composeRule.onNodeWithText("S.U.A.S. Veteran Support").assertIsDisplayed()
        composeRule.onNodeWithText("Temporary Shelter").performClick()
        composeRule.onNodeWithText("S.U.A.S. Veteran Support").assertIsDisplayed()
    }
}
