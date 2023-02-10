package io.opengood.project.sync.model

data class VersionAttributes(
    var type: String,
    var group: String,
    var groupPath: String,
    var name: String,
    var currentVersion: String,
    var newVersion: String,
) {
    companion object {
        val EMPTY = VersionAttributes(
            type = "",
            group = "",
            groupPath = "",
            name = "",
            currentVersion = "",
            newVersion = "",
        )
    }
}
