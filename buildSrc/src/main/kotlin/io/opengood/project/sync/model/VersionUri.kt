package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.VersionSourceType

data class VersionUri(
    val uri: String,
    val enabled: Boolean = true,
    val source: VersionSourceType,
    val pattern: String,
    val index: Int,
) {
    companion object {
        val EMPTY = VersionUri(
            uri = "",
            enabled = false,
            source = VersionSourceType.UNKNOWN,
            pattern = "",
            index = -1,
        )
    }
}
