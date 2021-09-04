package io.opengood.project.sync.constant

import java.util.regex.Pattern

class RegExs {

    companion object {
        val GRADLE_PLUGIN: Pattern = Pattern.compile("\\<h3\\>Version.*\\(latest\\).*\\<\\/h3\\>")
        val VERSION_NUMBER: Pattern = Pattern.compile("(?:(\\d+)\\.)?(?:(\\d+)\\.)?(?:(\\d+)\\.\\d+)")
        val GRADLE_WRAPPER: Pattern = Pattern.compile("\\<p\\>The current Gradle release is.*")
    }
}
