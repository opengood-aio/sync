package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.BuildGradleType
import io.opengood.project.sync.enumeration.BuildToolType
import io.opengood.project.sync.enumeration.LanguageType
import io.opengood.project.sync.enumeration.MavenFileType
import io.opengood.project.sync.enumeration.SettingsGradleType

data class BuildInfo(
    val language: LanguageType,
    val buildTool: BuildToolType,
    val buildGradle: BuildGradleType,
    val settingsGradle: SettingsGradleType,
    val mavenFile: MavenFileType
) {
    companion object {
        val EMPTY = BuildInfo(
            language = LanguageType.UNKNOWN,
            buildTool = BuildToolType.UNKNOWN,
            buildGradle = BuildGradleType.NONE,
            settingsGradle = SettingsGradleType.NONE,
            mavenFile = MavenFileType.NONE
        )
    }
}