package com.example.suas

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

/**
 * Pins the product launcher label: RootActivity shows "Veteran's Passport", the same
 * name as the iOS display name, instead of the template "MainActivity" title.
 * Plain JVM test; Gradle runs it with the app module as the working directory.
 */
class LauncherLabelTest {

    private fun parse(path: String): org.w3c.dom.Document {
        val file = File(path)
        assertTrue("missing $path", file.isFile)
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        return factory.newDocumentBuilder().parse(file)
    }

    private fun rootActivityLabel(): String {
        val activities = parse("src/main/AndroidManifest.xml").getElementsByTagName("activity")
        for (i in 0 until activities.length) {
            val activity = activities.item(i) as Element
            if (activity.getAttributeNS(ANDROID_NAMESPACE, "name") == ".RootActivity") {
                return activity.getAttributeNS(ANDROID_NAMESPACE, "label")
            }
        }
        throw AssertionError("RootActivity is not in the main manifest")
    }

    /** Resolves a string resource the way aapt does for a plain value: \' becomes '. */
    private fun stringResource(name: String): String {
        val strings = parse("src/main/res/values/strings.xml").getElementsByTagName("string")
        for (i in 0 until strings.length) {
            val string = strings.item(i) as Element
            if (string.getAttribute("name") == name) {
                return string.textContent.replace("\\'", "'")
            }
        }
        throw AssertionError("string resource $name is missing")
    }

    @Test
    fun rootActivityUsesTheProductLauncherString() {
        assertEquals("@string/title_activity_root", rootActivityLabel())
    }

    @Test
    fun productLauncherReadsVeteransPassport() {
        assertEquals("Veteran's Passport", stringResource("title_activity_root"))
    }
}
