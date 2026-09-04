package com.example.suas

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement

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
    fun mainScreenDoesNotExposeUnreleasedCrisisDirectionsAndShowsSupportOptions() {
        composeRule.onNodeWithText("911", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("988", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("Transportation").assertIsDisplayed()
        composeRule.onNodeWithText("Food").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Temporary Shelter").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("SUAS coordinates practical support. It is not an emergency service.")
            .performScrollTo().assertIsDisplayed()
    }

    @Test
    fun transportationFormAcceptsInputButCannotSubmit() {
        composeRule.onNodeWithText("Transportation").performScrollTo().performClick()
        composeRule.onNodeWithText("Confirm request").assertIsDisplayed()

        composeRule.onNodeWithText("855 W Maude Ave, Mountain View, CA")
            .performScrollTo().performTextReplacement("123 Main St")
        composeRule.onNodeWithText("500 Castro St, Mountain View, CA")
            .performScrollTo().performTextReplacement("VA Clinic")
        composeRule.onNodeWithText("Now")
            .performScrollTo().performTextReplacement("10:30 AM")

        composeRule.onNodeWithText("123 Main St").assertTextContains("123 Main St")
        composeRule.onNodeWithText("VA Clinic").assertTextContains("VA Clinic")
        composeRule.onNodeWithText("10:30 AM").assertTextContains("10:30 AM")
        composeRule.onNodeWithText("Submission not connected").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun transportationBackReturnsToMainScreen() {
        composeRule.onNodeWithText("Transportation").performScrollTo().performClick()
        composeRule.onNodeWithText("Cancel").assertIsEnabled().performClick()
        composeRule.onNodeWithText("S.U.A.S. Veteran Crisis Q.R.F.").assertIsDisplayed()
    }

    @Test
    fun unimplementedSupportCardsRemainOnMainScreen() {
        composeRule.onNodeWithText("Food").performScrollTo().performClick()
        composeRule.onNodeWithText("S.U.A.S. Veteran Crisis Q.R.F.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Temporary Shelter").performScrollTo().performClick()
        composeRule.onNodeWithText("S.U.A.S. Veteran Crisis Q.R.F.").performScrollTo().assertIsDisplayed()
    }
}
