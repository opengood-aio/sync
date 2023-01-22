package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.VersionProviderType

data class VersionExclusion(
    val description: String,
    val type: VersionProviderType,
    val group: String,
    val name: String,
    val versions: List<String>,
) {
    companion object {
        val EMPTY = VersionExclusion(
            description = "",
            type = VersionProviderType.UNKNOWN,
            group = "",
            name = "",
            versions = emptyList(),
        )
    }
}
