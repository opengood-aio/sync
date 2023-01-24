package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.VersionSourceType

data class VersionUri(
    val uri: String,
    val source: VersionSourceType,
    val pattern: String,
    val index: Int,
) {
    companion object {
        val EMPTY = VersionUri(
            uri = "",
            source = VersionSourceType.UNKNOWN,
            pattern = "",
            index = -1,
        )
    }
}
