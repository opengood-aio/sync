package io.opengood.project.sync.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GitConfig(
    @JsonProperty("commit-message")
    val commitMessage: String = "",
) {
    companion object {
        val DEFAULT_COMMIT_MESSAGE = "Perform automatic project sync changes"

        val EMPTY = GitConfig(
            commitMessage = "",
        )
    }
}
