package io.opengood.project.sync.model

data class VersionPatternResult(
    val key: String,
    val value: String,
) {
    companion object {
        val EMPTY = VersionPatternResult(
            key = "",
            value = "",
        )
    }
}
