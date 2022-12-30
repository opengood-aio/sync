package io.opengood.project.sync.model

data class VersionPattern(
    val index: Int,
    val key: String,
    val pattern: String,
) {
    companion object {
        val EMPTY = VersionPattern(
            index = -1,
            key = "",
            pattern = "",
        )
    }
}
