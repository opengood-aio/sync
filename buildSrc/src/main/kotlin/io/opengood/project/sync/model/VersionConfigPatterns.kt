package io.opengood.project.sync.model

import com.fasterxml.jackson.annotation.JsonProperty

data class VersionConfigPatterns(
    @JsonProperty("snapshot-version")
    val snapshotVersion: String,
    @JsonProperty("semantic-version")
    val semanticVersion: String,
    @JsonProperty("version-number")
    val versionNumber: String,
    @JsonProperty("version-number-ignore")
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
