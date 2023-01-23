package io.opengood.project.sync.model

data class VersionPattern(
    val key: String,
    val pattern: String,
    val index: Int,
    val trim: List<String> = emptyList(),
    val newLine: Boolean = false,
) {
    companion object {
        val EMPTY = VersionPattern(
            key = "",
            pattern = "",
            index = -1,
            trim = emptyList(),
            newLine = false,
        )
    }
}
