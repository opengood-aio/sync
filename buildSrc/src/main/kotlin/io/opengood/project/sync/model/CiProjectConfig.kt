package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.CiProviderType

data class CiProjectConfig(
    val provider: CiProviderType,
    val template: String,
) {
    companion object {
        val EMPTY = CiProjectConfig(
            provider = CiProviderType.UNKNOWN,
            template = "",
        )
    }
}
