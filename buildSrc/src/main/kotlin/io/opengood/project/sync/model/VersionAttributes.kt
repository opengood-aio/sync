package io.opengood.project.sync.model

data class VersionAttributes(
    var group: String,
    var groupPath: String,
    var name: String,
    var currentVersion: String,
    var newVersion: String,
) {
    companion object {
        val EMPTY = VersionAttributes(
            group = "",
            groupPath = "",
            name = "",
            currentVersion = "",
            newVersion = "",
        )
    }
}
