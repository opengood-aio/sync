package io.opengood.project.sync.model

data class GitInfo(
    val remote: String = "origin",
    val branch: String = "main"
) {
    companion object {
        val EMPTY = GitInfo(
            remote = "",
            branch = ""
        )
    }
}
