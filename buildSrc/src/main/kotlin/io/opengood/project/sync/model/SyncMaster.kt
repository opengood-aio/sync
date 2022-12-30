package io.opengood.project.sync.model

import java.io.File

data class SyncMaster(
    val version: String,
    val config: ConfigInfo = ConfigInfo(),
    val ci: CiMasterConfig,
) {
    lateinit var dir: File
    lateinit var file: File

    companion object {
        val EMPTY = SyncMaster(
            version = "",
            config = ConfigInfo.EMPTY,
            ci = CiMasterConfig.EMPTY,
        )
    }
}
