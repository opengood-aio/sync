package io.opengood.project.sync.model

data class CiMasterConfig(
    val providers: List<CiProvider>,
) {
    companion object {
        val EMPTY = CiMasterConfig(
            providers = emptyList()
        )
    }
}
