package io.opengood.project.sync.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GitInfo(
    val remote: String = "origin",
    val branch: String = "",
    @JsonProperty("commit-message")
    val commitMessage: String = "",
) {
    companion object {
        const val DEFAULT_COMMIT_MESSAGE = "Perform automatic project sync changes"

        val EMPTY =
            GitInfo(
                remote = "",
                branch = "",
                commitMessage = "",
            )
    }
}
