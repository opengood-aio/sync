package io.opengood.project.sync.model

import java.io.File

data class SyncMaster(
    val version: String,
    val ci: CiMasterConfig,
    val versioning: List<Version>
) {
    lateinit var dir: File
    lateinit var file: File

    companion object {
        val EMPTY = SyncMaster(
            version = "",
            ci = CiMasterConfig.EMPTY,
            versioning = emptyList()
        )
    }
}
