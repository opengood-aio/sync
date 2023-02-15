package io.opengood.project.sync.model

import java.io.File

data class SyncMaster(
    val version: String = "2.0",
    val config: ConfigInfo = ConfigInfo(),
    val ci: CiMasterConfig = CiMasterConfig(),
    val versions: VersionMasterConfig,
) {
    lateinit var dir: File
    lateinit var file: File

    companion object {
        val EMPTY = SyncMaster(
            version = "",
            config = ConfigInfo.EMPTY,
            ci = CiMasterConfig.EMPTY,
            versions = VersionMasterConfig.EMPTY,
        )
    }
}
