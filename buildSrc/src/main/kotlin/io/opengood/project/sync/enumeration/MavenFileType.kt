package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class MavenFileType(@JsonValue private val value: String) {
    POM("pom.xml"),
    NONE(""),
    ;

    override fun toString(): String = value
}
