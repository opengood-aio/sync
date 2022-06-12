package io.opengood.project.sync.model

data class  ConfigInfo(
    val enabled: Boolean = true
) {
    companion object {
        val EMPTY = ConfigInfo()
    }
}
