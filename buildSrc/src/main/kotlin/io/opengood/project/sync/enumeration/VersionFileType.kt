package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class VersionFileType(@JsonValue private val value: String) {
    BUILD_GRADLE("build.gradle"),
    DOCKERFILE("Dockerfile"),
    GRADLE_WRAPPER_PROPERTIES("gradle/wrapper/gradle-wrapper.properties"),
    MAVEN_WRAPPER_PROPERTIES(".mvn/wrapper/maven-wrapper.properties"),
    POM("pom.xml"),
    SETTINGS_GRADLE("settings.gradle"),
    VERSIONS_PROPERTIES("versions.properties"),
    ;

    override fun toString(): String = value
}
