package io.opengood.project.sync.model

data class VersionNumberAttributes(
    var current: String,
    var new: String,
) {
    companion object {
        val EMPTY =
            VersionNumberAttributes(
                current = "",
                new = "",
            )
    }
}
