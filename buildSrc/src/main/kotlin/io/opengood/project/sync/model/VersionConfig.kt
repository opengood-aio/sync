package io.opengood.project.sync.model

data class VersionConfig(
    val patterns: VersionConfigPatterns,
) {
    companion object {
        val EMPTY = VersionConfig(
            patterns = VersionConfigPatterns.EMPTY,
        )
    }
}
