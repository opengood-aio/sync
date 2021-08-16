package io.opengood.project.sync.model

import java.io.File

data class SyncMetadata(
    val dir: File,
    val syncFile: File
)
