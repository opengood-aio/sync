package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class BuildToolType(@JsonValue private val value: String) {
    GRADLE("gradle"),
    MAVEN("maven"),
    UNKNOWN(""),
    ;

    override fun toString(): String = value
}
