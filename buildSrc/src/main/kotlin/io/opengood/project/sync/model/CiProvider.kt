package io.opengood.project.sync.model

import io.opengood.project.sync.enumeration.CiProviderType

data class CiProvider(
    val name: CiProviderType,
    val template: CiTemplate,
)
