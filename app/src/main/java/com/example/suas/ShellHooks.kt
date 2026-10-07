package com.example.suas

import com.example.suas.api.ActiveRequest
import com.example.suas.api.ClientPins

/**
 * Optional hooks a debug-only launcher passes to the product shell.
 *
 * The release launcher (RootActivity) always uses [NONE], so release builds show no banner,
 * no sign-in hint, and no demo controls. Debug launchers live in src/debug and are absent
 * from release builds.
 */
class ShellHooks(
    /** Environment class shown in the build info block. Explicit, never derived from the URL. */
    val environmentClass: String = ClientPins.ENVIRONMENT_CLASS,
    /** Short banner on the home screen, for example "Demo mode: synthetic data, no server". */
    val banner: String? = null,
    /** Extra line on the sign-in screen, for example the synthetic demo email and code. */
    val signInHint: String? = null,
    /** Email prefilled on the sign-in screen. Empty in every real path. */
    val prefillEmail: String = "",
    /** Returns an existing request for a category so a demo screen opens populated. */
    val existingRequest: ((authorization: String, category: String) -> ActiveRequest?)? = null,
    /** Moves a request one status step and returns the new status (demo stand-in for LOCAL simulate). */
    val advance: ((serviceRequestId: String) -> String?)? = null,
) {
    companion object {
        val NONE = ShellHooks()
    }
}
