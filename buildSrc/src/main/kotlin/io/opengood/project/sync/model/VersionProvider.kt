package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.BuildToolType
import io.opengood.project.sync.enumeration.VersionProviderType

data class VersionProvider(
    val name: String,
    val type: VersionProviderType,
    val tools: List<BuildToolType>,
    val files: List<String>,
    val read: List<VersionPattern>,
    val uris: List<VersionUri>,
    val write: List<VersionPattern>,
) {
    companion object {
        val EMPTY = VersionProvider(
            name = "",
            type = VersionProviderType.UNKNOWN,
            tools = emptyList(),
            files = emptyList(),
            read = emptyList(),
            uris = emptyList(),
            write = emptyList(),
        )
    }
}
