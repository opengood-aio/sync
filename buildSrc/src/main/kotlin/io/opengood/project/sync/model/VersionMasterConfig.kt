package io.opengood.project.sync.model

import java.io.File

data class VersionMasterConfig(
    val config: VersionConfig,
    val providers: List<VersionProvider>,
) {
    companion object {
        val EMPTY = VersionMasterConfig(
            config = VersionConfig.EMPTY,
            providers = emptyList(),
        )
    }
}
