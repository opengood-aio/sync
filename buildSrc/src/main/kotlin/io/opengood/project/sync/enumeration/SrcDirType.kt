package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class SrcDirType(@JsonValue val values: List<String>) {
    GROOVY(listOf("src/main/groovy", "buildSrc/src/main/groovy")),
    JAVA(listOf("src/main/java", "buildSrc/src/main/java")),
    KOTLIN(listOf("src/main/kotlin", "buildSrc/src/main/kotlin")),
    ;

    override fun toString() = values.joinToString(",")
}