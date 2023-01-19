package io.opengood.project.sync.model

import java.io.File

data class VersionProjectConfig(
    val exclusions: List<VersionExclusion>,
) {
    companion object {
        val EMPTY = VersionProjectConfig(
            exclusions = emptyList(),
        )
    }
}
