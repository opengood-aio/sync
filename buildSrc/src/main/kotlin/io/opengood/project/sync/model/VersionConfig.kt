package io.opengood.project.sync.model

data class VersionConfig(
    val git: GitInfo,
    val patterns: VersionConfigPatterns,
) {
    companion object {
        val EMPTY = VersionConfig(
            git = GitInfo.EMPTY,
            patterns = VersionConfigPatterns.EMPTY,
        )
    }
}
