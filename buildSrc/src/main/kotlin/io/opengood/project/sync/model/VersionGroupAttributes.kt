package io.opengood.project.sync.model

data class VersionGroupAttributes(
    var group: String,
    var path: String,
) {
    companion object {
        val EMPTY =
            VersionGroupAttributes(
                group = "",
                path = "",
            )
    }
}
