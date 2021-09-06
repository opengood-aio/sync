package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class BuildGradleType(@JsonValue private val value: String) {
    GROOVY("build.gradle"),
    KOTLIN("build.gradle.kts"),
    NONE(""),
    ;

    override fun toString(): String = value
}
