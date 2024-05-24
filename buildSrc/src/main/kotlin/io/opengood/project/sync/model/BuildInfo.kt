package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.BuildToolType
import io.opengood.project.sync.enumeration.FileType
import io.opengood.project.sync.enumeration.LanguageType

data class BuildInfo(
    val language: LanguageType,
    val tool: BuildToolType,
    val files: List<FileType>,
) {
    companion object {
        val EMPTY = BuildInfo(
            language = LanguageType.UNKNOWN,
            tool = BuildToolType.UNKNOWN,
            files = emptyList(),
        )
    }
}
