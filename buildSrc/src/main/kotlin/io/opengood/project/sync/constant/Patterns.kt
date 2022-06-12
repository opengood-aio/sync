package io.opengood.project.sync.constant

import java.util.regex.Pattern

class Patterns {

    companion object {
        val DOCKER_IMAGE: Pattern = Pattern.compile("\$.tags")
        val GRADLE_WRAPPER: Pattern = Pattern.compile("\$.version")
        val MAVEN_DEPENDENCY: Pattern = Pattern.compile("//metadata/versioning/versions/version")
        val NEXUS_DEPENDENCY: Pattern = Pattern.compile("\$.items")
        val SEMANTIC_VERSION: Pattern = Pattern.compile("(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)")
        val VERSION_NUMBER: Pattern = Pattern.compile("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.?(0|[1-9]\\d*)?(?:(-|\\.)((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?")
        val VERSION_NUMBER_IGNORE = listOf("alpha", "beta", "rc", "dev")
    }
}
