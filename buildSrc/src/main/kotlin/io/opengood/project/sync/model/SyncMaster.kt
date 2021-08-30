package io.opengood.project.sync.model

import java.io.File

data class SyncMaster(
    val version: String,
    val ci: CiMasterConfig,
    val plugins: List<Plugin>
) {
    lateinit var dir: File
    lateinit var file: File
}
