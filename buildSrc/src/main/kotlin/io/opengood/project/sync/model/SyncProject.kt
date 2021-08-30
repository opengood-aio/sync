package io.opengood.project.sync.model

import java.io.File

data class SyncProject(
    val version: String,
    val ci: CiConfig,
) {
    lateinit var name: String
    lateinit var dir: File
    lateinit var file: File
}
