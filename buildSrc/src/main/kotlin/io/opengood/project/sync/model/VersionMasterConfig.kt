package io.opengood.project.sync.model

data class VersionMasterConfig(
    val config: VersionConfig,
    val exclusions: List<VersionExclusion> = emptyList(),
    val providers: List<VersionProvider>,
) {
    companion object {
        val EMPTY =
            VersionMasterConfig(
                config = VersionConfig.EMPTY,
                exclusions = emptyList(),
                providers = emptyList(),
            )
    }
}
