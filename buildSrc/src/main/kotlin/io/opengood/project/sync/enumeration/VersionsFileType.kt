package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class VersionsFileType(@JsonValue private val value: String) {
    SETTINGS_GRADLE("settings.gradle"),
    VERSION_PROPERTIES("versions.properties"),
    ;

    override fun toString(): String = value
}
