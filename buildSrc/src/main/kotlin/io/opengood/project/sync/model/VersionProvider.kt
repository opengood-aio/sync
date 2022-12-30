package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.BuildToolType
import io.opengood.project.sync.enumeration.VersionProviderType

data class VersionProvider(
    val name: VersionProviderType,
    val tools: List<BuildToolType>,
    val files: List<String>,
    val read: List<VersionPattern>,
    val uris: List<String>,
    val parse: List<VersionPattern>,
    val write: List<VersionPattern>,
) {
    companion object {
        val EMPTY = VersionProvider(
            name = VersionProviderType.UNKNOWN,
            tools = emptyList(),
            files = emptyList(),
            read = emptyList(),
            uris = emptyList(),
            parse = emptyList(),
            write = emptyList(),
        )
    }
}
