package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.BuildToolType
import io.opengood.project.sync.enumeration.FileType
import io.opengood.project.sync.enumeration.VersionProviderType

data class VersionProvider(
    val name: String,
    val enabled: Boolean = true,
    val types: List<VersionProviderType>,
    val tools: List<BuildToolType>,
    val files: List<FileType>,
    val read: List<VersionPattern>,
    val uris: List<VersionUri>,
    val write: List<VersionPattern>,
) {
    companion object {
        val EMPTY = VersionProvider(
            name = "",
            enabled = false,
            types = emptyList(),
            tools = emptyList(),
            files = emptyList(),
            read = emptyList(),
            uris = emptyList(),
            write = emptyList(),
        )
    }
}
