package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.VersioningType
import io.opengood.project.sync.enumeration.VersionsFileType

data class Version(
    val name: String,
    val type: VersioningType,
    val uri: String,
    val key: String,
    val file: VersionsFileType,
    val pattern: String
)
