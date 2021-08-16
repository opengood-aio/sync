package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.CiProviderType

data class CiConfig(
    val providerType: CiProviderType,
    val template: String
)
