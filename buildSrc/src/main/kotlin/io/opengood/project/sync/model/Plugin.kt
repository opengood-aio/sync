package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.PluginType
import io.opengood.project.sync.enumeration.VersionsFileType

data class Plugin(
    val name: String,
    val type: PluginType,
    val uri: String,
    val key: String,
    val file: VersionsFileType
)
