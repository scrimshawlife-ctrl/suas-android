package com.example.suas

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.w3c.dom.Element

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

/**
 * Keeps the dummy MainActivity form labeled and confined as a debug test harness.
 * Covers SUAS-specs REMAINING.md hygiene item 2.
 */
class MainActivityHarnessTest {

    @Test
    fun noticeSaysTestHarnessOnly() {
        assertTrue(TEST_HARNESS_NOTICE.startsWith("TEST HARNESS ONLY"))
        assertTrue(TEST_HARNESS_NOTICE.contains("RootActivity"))
        assertTrue(TEST_HARNESS_NOTICE.contains("/api/v0"))
        assertFalse(TEST_HARNESS_NOTICE.contains("\u2014"))
    }

    @Test
    fun debugManifestKeepsMainActivityPrivate() {
        val debugActivities = parseActivities(File("src/debug/AndroidManifest.xml"))
        val mainActivity = debugActivities["MainActivity"]
        assertTrue("Debug manifest must declare MainActivity", mainActivity != null)
        val activity = mainActivity!!
        assertEquals("false", activity.getAttributeNS(ANDROID_NS, "exported"))
        assertFalse("MainActivity must not have an intent-filter", hasChildIntentFilter(activity))
    }

    @Test
    fun mainManifestHasNoMainActivity() {
        val mainActivities = parseActivities(File("src/main/AndroidManifest.xml"))
        assertFalse(mainActivities.containsKey("MainActivity"))
    }

    @Test
    fun productLauncherIsRootActivity() {
        val mainActivities = parseActivities(File("src/main/AndroidManifest.xml"))
        val launcherNames = mainActivities.filterValues { hasMainLauncherIntentFilter(it) }.keys
        assertEquals(setOf("RootActivity"), launcherNames)
    }

    @Test
    fun onlyDemoAndLocalAreDebugLaunchers() {
        val debugActivities = parseActivities(File("src/debug/AndroidManifest.xml"))
        val launcherNames = debugActivities.filterValues { hasMainLauncherIntentFilter(it) }.keys
        assertEquals(setOf("DemoRootActivity", "LocalRootActivity"), launcherNames)
    }

    private fun parseActivities(manifestFile: File): Map<String, Element> {
        if (!manifestFile.isFile) {
            fail("Manifest not found: ${manifestFile.absolutePath}")
        }

        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val document = factory.newDocumentBuilder().parse(manifestFile)

        val activityNodes = document.getElementsByTagName("activity")
        val activities = LinkedHashMap<String, Element>()

        for (i in 0 until activityNodes.length) {
            val element = activityNodes.item(i) as? Element ?: continue
            val rawName = element.getAttributeNS(ANDROID_NS, "name")
            val normalizedName = rawName.removePrefix(".").removePrefix("com.example.suas.")
            activities[normalizedName] = element
        }

        return activities
    }

    private fun hasChildIntentFilter(activity: Element): Boolean {
        val children = activity.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i) as? Element ?: continue
            if (child.tagName == "intent-filter") {
                return true
            }
        }
        return false
    }

    private fun hasMainLauncherIntentFilter(activity: Element): Boolean {
        val filters = activity.getElementsByTagName("intent-filter")
        for (i in 0 until filters.length) {
            val filter = filters.item(i) as? Element ?: continue
            if (filterContainsMainAction(filter) && filterContainsLauncherCategory(filter)) {
                return true
            }
        }
        return false
    }

    private fun filterContainsMainAction(filter: Element): Boolean {
        val actions = filter.getElementsByTagName("action")
        for (i in 0 until actions.length) {
            val action = actions.item(i) as? Element ?: continue
            if (action.getAttributeNS(ANDROID_NS, "name") == "android.intent.action.MAIN") {
                return true
            }
        }
        return false
    }

    private fun filterContainsLauncherCategory(filter: Element): Boolean {
        val categories = filter.getElementsByTagName("category")
        for (i in 0 until categories.length) {
            val category = categories.item(i) as? Element ?: continue
            if (category.getAttributeNS(ANDROID_NS, "name") == "android.intent.category.LAUNCHER") {
                return true
            }
        }
        return false
    }
}
