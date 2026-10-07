package com.example.suas.api

/**
 * Explicit client pins. The environment class is not derived from the URL
 * or from debug/release. SUAS-specs MOBILE_SURFACE.md §8 and ENVIRONMENT.md §2.
 *
 * SPEC-018 remains KEEP_BLOCKED. applicationId com.example.suas is a placeholder.
 */
object ClientPins {
    const val ENVIRONMENT_CLASS = "STAGING"
    const val API_BASE_URL = "https://suasqrf.com"
    const val SPEC_VERSION = "0.6.0"
    const val RELEASE_MANIFEST = "RELEASE_MANIFEST-0.6.0.md"
    const val ALLOW_REAL_EXTERNAL_EFFECTS = false
    /** Must equal versionName in app/build.gradle.kts (ContractTest checks). Pre-1.0: SPEC-018 blocked. */
    const val APPLICATION_VERSION = "0.1.0"
    const val COMMIT = "SOURCE_TREE"
    const val BUILD_TIMESTAMP = "SOURCE_TREE"
    const val APPLICATION_ID_STATUS = "PLACEHOLDER_NOT_RELEASED"
}

data class BuildInfo(
    val appVersion: String,
    val commit: String,
    val specVersion: String,
    val manifest: String,
    val buildTimestamp: String,
    val environmentClass: String,
)

sealed class ConfigurationDecision {
    data class Accepted(val info: BuildInfo) : ConfigurationDecision()
    data class Rejected(val reason: String) : ConfigurationDecision()
}

object ClientConfiguration {
    const val PINNED_SPEC = "0.6.0"
    const val PINNED_MANIFEST = "RELEASE_MANIFEST-0.6.0.md"
    const val STAGING_HOST = "suasqrf.com"
    val LOCAL_HOSTS = setOf("localhost", "127.0.0.1", "10.0.2.2")

    fun validate(
        environmentClass: String = ClientPins.ENVIRONMENT_CLASS,
        apiBaseUrl: String = ClientPins.API_BASE_URL,
        specVersion: String = ClientPins.SPEC_VERSION,
        releaseManifest: String = ClientPins.RELEASE_MANIFEST,
        allowRealExternalEffects: Boolean = ClientPins.ALLOW_REAL_EXTERNAL_EFFECTS,
        applicationVersion: String = ClientPins.APPLICATION_VERSION,
        commit: String = ClientPins.COMMIT,
        buildTimestamp: String = ClientPins.BUILD_TIMESTAMP,
    ): ConfigurationDecision {
        if (environmentClass !in setOf("LOCAL", "TEST", "STAGING", "PRODUCTION")) {
            return ConfigurationDecision.Rejected("Unknown environment class $environmentClass.")
        }
        if (specVersion != PINNED_SPEC) {
            return ConfigurationDecision.Rejected("Spec pin $specVersion does not match $PINNED_SPEC.")
        }
        if (releaseManifest != PINNED_MANIFEST) {
            return ConfigurationDecision.Rejected("Manifest pin $releaseManifest does not match $PINNED_MANIFEST.")
        }
        if (allowRealExternalEffects) {
            return ConfigurationDecision.Rejected(
                "Real external effects are invalid. SPEC-018 is KEEP_BLOCKED, so the flag is rejected in every environment class.",
            )
        }
        val scheme = apiBaseUrl.substringBefore("://", missingDelimiterValue = "").lowercase()
        val host = apiBaseUrl.substringAfter("://", missingDelimiterValue = "")
            .substringBefore('/')
            .substringBefore(':')
            .lowercase()
        if (host.isEmpty()) {
            return ConfigurationDecision.Rejected("API base URL has no host.")
        }
        when (environmentClass) {
            "LOCAL", "TEST" -> {
                if (host !in LOCAL_HOSTS) {
                    return ConfigurationDecision.Rejected("$environmentClass is not paired with host $host.")
                }
                if (scheme != "http" && scheme != "https") {
                    return ConfigurationDecision.Rejected("$environmentClass URL scheme $scheme is not allowed.")
                }
            }
            "STAGING" -> {
                if (host != STAGING_HOST) {
                    return ConfigurationDecision.Rejected("STAGING is paired only with $STAGING_HOST.")
                }
                if (scheme != "https") {
                    return ConfigurationDecision.Rejected("STAGING requires https.")
                }
            }
            "PRODUCTION" -> {
                return ConfigurationDecision.Rejected(
                    "PRODUCTION has no released host. SPEC-018 is KEEP_BLOCKED, so a production configuration is rejected.",
                )
            }
        }
        return ConfigurationDecision.Accepted(
            BuildInfo(
                appVersion = applicationVersion,
                commit = commit,
                specVersion = specVersion,
                manifest = releaseManifest,
                buildTimestamp = buildTimestamp,
                environmentClass = environmentClass,
            ),
        )
    }
}
