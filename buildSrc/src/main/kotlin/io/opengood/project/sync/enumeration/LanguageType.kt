package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class LanguageType(
    @JsonValue private val value: String,
) {
    GROOVY("Groovy"),
    JAVA("Java"),
    KOTLIN("Kotlin"),
    UNKNOWN("Unknown"),
    ;

    override fun toString() = value
}
