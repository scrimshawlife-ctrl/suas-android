package com.example.suas.api

/**
 * Released D-012 wording. SAFETY_COPY.md §0 and §1.1.
 * Destinations are 911 and 988 only. Nothing in this object places a call.
 */
object CrisisCopy {
    const val ENTRY_HEADING = "Need help right now?"
    const val IMMEDIATE_DANGER =
        "If you are in immediate danger, have a medical emergency, or believe someone may be seriously harmed, call 911 or go to the nearest emergency department."
    const val NOT_EMERGENCY_SERVICE =
        "SUAS coordinates practical support. It is not an emergency service and cannot replace police, fire, EMS, or emergency medical care."
    const val LIFELINE =
        "If you are experiencing suicidal thoughts, severe emotional distress, or a mental health crisis, call or text the 988 Suicide & Crisis Lifeline at 988."
    const val VETERANS_CRISIS_LINE = "Veterans reach the Veterans Crisis Line through 988."
    const val ACTION_911 = "Call 911"
    const val ACTION_988 = "Call or text 988"
    const val CALL_911 = "tel:911"
    const val CALL_988 = "tel:988"
    const val TEXT_988 = "sms:988"

    val destinations = listOf(CALL_911, CALL_988, TEXT_988)
}

object ListAccess {
    const val FOLLOWS_NEXT_CURSOR = false
}

object BackupPolicy {
    const val ALLOW_BACKUP = false
}
