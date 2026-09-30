package com.example.suas.api

import com.google.gson.annotations.SerializedName

/**
 * Read of the latest server list. Nothing here is stored.
 * CONSENT.md §6 template copy is NOT_COMPUTABLE, so this file does not capture a grant.
 */
data class ConsentGrantDto(
    val permission: String? = null,
    val scope: String? = null,
    val status: String? = null,
    @SerializedName("grantee_id") val granteeId: String? = null,
)

data class ConsentListDto(
    val consents: List<ConsentGrantDto> = emptyList(),
)

fun disclosureAllowed(
    latest: List<ConsentGrantDto>,
    permission: String,
    scope: String,
    granteeId: String,
): Boolean = latest.any {
    it.status == "ACTIVE" &&
        it.permission == permission &&
        it.scope == scope &&
        it.granteeId == granteeId
}

/** A second server read replaces the first. The decision keeps no list of its own. */
fun disclosureAllowed(latest: ConsentListDto, permission: String, scope: String, granteeId: String): Boolean =
    disclosureAllowed(latest.consents, permission, scope, granteeId)
