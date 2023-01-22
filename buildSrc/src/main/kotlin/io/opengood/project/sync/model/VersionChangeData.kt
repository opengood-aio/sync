package io.opengood.project.sync.model

data class VersionChangeData(
    val line: VersionLineData,
    val attributes: VersionAttributes,
    val exclusions: List<VersionExclusion>,
    val patterns: VersionConfigPatterns,
    val provider: VersionProvider,
) {
    companion object {
        val EMPTY = VersionChangeData(
            line = VersionLineData.EMPTY,
            attributes = VersionAttributes.EMPTY,
            exclusions = emptyList(),
            patterns = VersionConfigPatterns.EMPTY,
            provider = VersionProvider.EMPTY,
        )
    }
}
