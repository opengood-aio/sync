package io.opengood.project.sync.model

import java.io.File

data class SyncProject(
    val version: String,
    val config: ConfigInfo = ConfigInfo(),
    val git: GitInfo = GitInfo(),
    val ci: CiProjectConfig,
) {
    lateinit var name: String
    lateinit var dir: File
    lateinit var file: File

    companion object {
        val EMPTY = SyncProject(
            version = "",
            config = ConfigInfo.EMPTY,
            git = GitInfo.EMPTY,
            ci = CiProjectConfig.EMPTY,
        )
    }
}
