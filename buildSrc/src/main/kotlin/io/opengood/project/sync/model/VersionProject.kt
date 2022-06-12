package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.VersionType

data class VersionProject(
    val id: String,
    val type: VersionType,
    val inclusions: List<String>
)
