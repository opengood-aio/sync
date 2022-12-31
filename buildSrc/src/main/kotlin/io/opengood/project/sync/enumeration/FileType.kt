package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class FileType(@JsonValue private val value: String) {
    BUILD_GRADLE_GROOVY("build.gradle"),
    BUILD_GRADLE_KOTLIN("build.gradle.kts"),
    DOCKERFILE("Dockerfile"),
    GRADLE_WRAPPER_PROPERTIES("gradle/wrapper/gradle-wrapper.properties"),
    MAVEN_POM("pom.xml"),
    MAVEN_WRAPPER_PROPERTIES(".mvn/wrapper/maven-wrapper.properties"),
    SETTINGS_GRADLE_GROOVY("settings.gradle"),
    SETTINGS_GRADLE_KOTLIN("settings.gradle.kts"),
    VERSIONS_PROPERTIES("versions.properties"),
    UNKNOWN("unknown"),
    ;

    override fun toString(): String = value
}
