package io.opengood.project.sync.model

data class VersionPattern(
    val key: String,
    val pattern: String = "",
    val index: Int = 0,
    var trim: MutableList<String> = mutableListOf(),
    val newLine: Boolean = false,
    val value: String = "",
) {
    companion object {
        val EMPTY =
            VersionPattern(
                key = "",
                pattern = "",
                index = -1,
                trim = mutableListOf(),
                newLine = false,
                value = "",
            )
    }
}
