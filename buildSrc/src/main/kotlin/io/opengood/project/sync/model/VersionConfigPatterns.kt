package io.opengood.project.sync.model

data class VersionConfigPatterns(
    val snapshotVersion: String,
    val semanticVersion: String,
    val versionNumber: String,
    val versionNumberIgnore: List<String>,
) {
    companion object {
        val EMPTY = VersionConfigPatterns(
            snapshotVersion = "",
            semanticVersion = "",
            versionNumber = "",
            versionNumberIgnore = emptyList(),
        )
    }
}
