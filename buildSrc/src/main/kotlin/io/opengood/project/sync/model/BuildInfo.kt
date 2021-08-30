package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.BuildGradleType
import io.opengood.project.sync.enumeration.LanguageType
import io.opengood.project.sync.enumeration.SettingsGradleType

data class BuildInfo(
    val language: LanguageType,
    val buildGradle: BuildGradleType,
    val settingsGradle: SettingsGradleType
)
