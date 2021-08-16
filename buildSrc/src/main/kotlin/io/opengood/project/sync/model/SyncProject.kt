package io.opengood.project.sync.model

data class SyncProject(
    val version: String,
    val project: ProjectConfig,
    val ci: CiConfig,
) {
    lateinit var name: String
    lateinit var metadata: SyncMetadata
}
