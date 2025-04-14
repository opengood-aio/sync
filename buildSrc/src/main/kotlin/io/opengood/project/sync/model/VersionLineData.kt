package io.opengood.project.sync.model

data class VersionLineData(
    val currentLine: String,
    val spaces: Int,
    val prevLine: String,
    val priorLine: String,
) {
    companion object {
        val EMPTY =
            VersionLineData(
                currentLine = "",
                spaces = -1,
                prevLine = "",
                priorLine = "",
            )
    }
}
