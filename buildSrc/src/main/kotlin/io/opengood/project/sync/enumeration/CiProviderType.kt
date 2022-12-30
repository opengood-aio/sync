package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class CiProviderType(@JsonValue private val value: String) {
    GITHUB_ACTIONS("GitHub Actions"),
    UNKNOWN(""),
    ;

    override fun toString() = value
}
