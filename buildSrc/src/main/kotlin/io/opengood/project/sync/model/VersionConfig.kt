package io.opengood.project.sync.model

data class VersionConfig(
    val git: GitConfig,
    val patterns: VersionConfigPatterns,
) {
    companion object {
        val EMPTY = VersionConfig(
            git = GitConfig.EMPTY,
            patterns = VersionConfigPatterns.EMPTY,
        )
    }
}
