package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class VersionSourceType(@JsonValue private val value: String) {
    GRADLE_SERVICES("Gradle Services"),
    MAVEN_CENTRAL("Maven Central"),
    NEXUS_HOSTED_REPO("Nexus Hosted Repo"),
    NEXUS_PROXY_REPO("Nexus Proxy Repo"),
    UNKNOWN("Unknown"),
    ;

    override fun toString() = value
}
