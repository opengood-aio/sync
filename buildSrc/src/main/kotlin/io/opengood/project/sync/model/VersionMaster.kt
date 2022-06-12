package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.BuildToolType
import io.opengood.project.sync.enumeration.VersionType
import io.opengood.project.sync.enumeration.VersionFileType

data class VersionMaster(
    val name: String,
    val id: String,
    val tools: List<BuildToolType>,
    val type: VersionType,
    val uri: String,
    val key: String,
    val subKey: String = "",
    val files: List<VersionFileType>,
    val pattern: String,
    val inclusions: List<String> = emptyList()
)
