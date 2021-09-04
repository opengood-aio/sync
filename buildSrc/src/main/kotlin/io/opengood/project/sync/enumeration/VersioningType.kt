package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class VersioningType(@JsonValue private val value: String) {
    GRADLE_PLUGIN("Gradle Plugin"),
    GRADLE_WRAPPER("Gradle Wrapper"),
    ;

    override fun toString() = value
}
