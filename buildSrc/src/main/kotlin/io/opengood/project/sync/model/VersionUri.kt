package io.opengood.project.sync.model

data class VersionUri(
    val index: Int,
    val uri: String,
    val pattern: String,
) {
    companion object {
        val EMPTY = VersionUri(
            index = -1,
            uri = "",
            pattern = "",
        )
    }
}
