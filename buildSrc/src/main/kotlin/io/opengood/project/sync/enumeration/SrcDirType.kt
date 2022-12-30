package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class SrcDirType(@JsonValue private val value: String) {
    GROOVY("src/main/groovy"),
    JAVA("src/main/java"),
    KOTLIN("src/main/kotlin"),
    ;

    override fun toString() = value
}