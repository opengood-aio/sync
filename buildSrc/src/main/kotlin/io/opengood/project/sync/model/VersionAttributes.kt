package io.opengood.project.sync.model

data class VersionAttributes(
    var group: VersionGroupAttributes,
    var id: String,
    var key: String,
    var name: String,
    var uri: String,
    var version: VersionNumberAttributes,
) {
    companion object {
        val EMPTY = VersionAttributes(
            group = VersionGroupAttributes.EMPTY,
            id = "",
            key = "",
            name = "",
            uri = "",
            version = VersionNumberAttributes.EMPTY,
        )
    }
}
