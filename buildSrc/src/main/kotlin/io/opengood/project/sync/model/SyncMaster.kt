package io.opengood.project.sync.model

data class SyncMaster(
    val version: String,
    val ci: CiMasterConfig
) {
    lateinit var metadata: SyncMetadata
}
