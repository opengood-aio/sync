package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.BuildType
import io.opengood.project.sync.enumeration.LanguageType

data class ProjectConfig(
    val buildType: BuildType,
    val languageType: LanguageType
)
