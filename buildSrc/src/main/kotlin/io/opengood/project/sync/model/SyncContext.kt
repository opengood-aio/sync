package io.opengood.project.sync.model

import java.io.File

data class SyncContext(
    val workspaceDir: File,
    val syncProjectDir: File,
)
