package io.opengood.project.sync.model

data class VersionUri(
    val uri: String,
    val pattern: String,
    val index: Int,
) {
    companion object {
        val EMPTY = VersionUri(
            uri = "",
            pattern = "",
            index = -1,
        )
    }
}
