package io.opengood.project.sync.model

data class CiMasterConfig(
    val providers: List<CiProvider> = emptyList(),
) {
    companion object {
        val EMPTY = CiMasterConfig(
            providers = emptyList()
        )
    }
}
