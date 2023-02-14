package io.opengood.project.sync.model

import com.fasterxml.jackson.annotation.JsonProperty

data class VersionConfigPatterns(
    @JsonProperty("dev-version")
    val devVersion: String,
    @JsonProperty("semantic-version")
    val semanticVersion: String,
    @JsonProperty("version-number")
    val versionNumber: String,
    @JsonProperty("version-number-ignore")
    val versionNumberIgnore: List<String>,
    @JsonProperty("version-placeholder")
    val versionPlaceholder: String,
) {
    companion object {
        val EMPTY = VersionConfigPatterns(
            devVersion = "",
            semanticVersion = "",
            versionNumber = "",
            versionNumberIgnore = emptyList(),
            versionPlaceholder = ""
        )
    }
}
