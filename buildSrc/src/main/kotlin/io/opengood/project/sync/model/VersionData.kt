package io.opengood.project.sync.model

import java.io.File

data class VersionData(
    val config: VersionConfig,
    val exclusions: List<VersionExclusion>,
    val provider: VersionProvider,
) {
    companion object {
        val EMPTY = VersionData(
            config = VersionConfig.EMPTY,
            exclusions = emptyList(),
            provider = VersionProvider.EMPTY,
        )
    }
}
