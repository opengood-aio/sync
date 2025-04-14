package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class VersionProviderType(
    @JsonValue private val value: String,
) {
    DOCKER_IMAGE("Docker Image"),
    GRADLE_DEPENDENCY("Gradle Dependency"),
    GRADLE_NEXUS_DEPENDENCY("Gradle Nexus Dependency"),
    GRADLE_PLUGIN("Gradle Plugin"),
    GRADLE_WRAPPER("Gradle Wrapper"),
    MAVEN_DEPENDENCY("Maven Dependency"),
    MAVEN_NEXUS_DEPENDENCY("Maven Nexus Dependency"),
    MAVEN_PLUGIN("Maven Plugin"),
    MAVEN_WRAPPER("Maven Wrapper"),
    UNKNOWN("Unknown"),
    ;

    override fun toString() = value
}
