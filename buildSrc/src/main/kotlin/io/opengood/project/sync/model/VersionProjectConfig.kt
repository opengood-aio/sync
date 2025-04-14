package io.opengood.project.sync.model

data class VersionProjectConfig(
    val config: VersionConfig,
    val exclusions: List<VersionExclusion>,
) {
    companion object {
        val EMPTY =
            VersionProjectConfig(
                config = VersionConfig.EMPTY,
                exclusions = emptyList(),
            )
    }
}
