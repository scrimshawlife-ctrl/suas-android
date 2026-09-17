package com.example.suas.api

/**
 * Build-pinned host and synthetic tenant.
 * The person does not pick a tenant. JSON auth still requires tenant_id on the
 * wire (Worker issueBody). This constant is the LOCAL/STAGING seed, not a picker.
 */
object Backend {
    const val STAGING_BASE = "https://suasqrf.com"
    const val LOCAL_BASE = "http://10.0.2.2:3000"
    const val API_PREFIX = "/api/v0"
    const val SYNTHETIC_TENANT_ID = "00000000-0000-4000-8000-000000000001"
}
