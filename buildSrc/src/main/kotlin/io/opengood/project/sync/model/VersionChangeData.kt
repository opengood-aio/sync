package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.FileType

data class VersionChangeData(
    val file: FileType,
    val line: VersionLineData,
    val attributes: VersionAttributes,
    val exclusions: List<VersionExclusion>,
    val patterns: VersionConfigPatterns,
    val provider: VersionProvider,
) {
    companion object {
        val EMPTY = VersionChangeData(
            file = FileType.UNKNOWN,
            line = VersionLineData.EMPTY,
            attributes = VersionAttributes.EMPTY,
            exclusions = emptyList(),
            patterns = VersionConfigPatterns.EMPTY,
            provider = VersionProvider.EMPTY,
        )
    }
}
