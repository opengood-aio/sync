package io.opengood.project.sync.task

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.jayway.jsonpath.JsonPath
import io.opengood.project.sync.containsAny
import io.opengood.project.sync.countSpaces
import io.opengood.project.sync.enumeration.BuildToolType.GRADLE
import io.opengood.project.sync.enumeration.BuildToolType.MAVEN
import io.opengood.project.sync.enumeration.FileType.BUILD_GRADLE_GROOVY
import io.opengood.project.sync.enumeration.FileType.BUILD_GRADLE_KOTLIN
import io.opengood.project.sync.enumeration.FileType.GRADLE_WRAPPER_PROPERTIES
import io.opengood.project.sync.enumeration.FileType.MAVEN_POM
import io.opengood.project.sync.enumeration.FileType.MAVEN_WRAPPER_PROPERTIES
import io.opengood.project.sync.enumeration.FileType.SETTINGS_GRADLE_GROOVY
import io.opengood.project.sync.enumeration.FileType.SETTINGS_GRADLE_KOTLIN
import io.opengood.project.sync.enumeration.FileType.VERSIONS_PROPERTIES
import io.opengood.project.sync.enumeration.VersionProviderType.GRADLE_DEPENDENCY
import io.opengood.project.sync.enumeration.VersionProviderType.GRADLE_NEXUS_DEPENDENCY
import io.opengood.project.sync.enumeration.VersionProviderType.GRADLE_PLUGIN
import io.opengood.project.sync.enumeration.VersionProviderType.GRADLE_WRAPPER
import io.opengood.project.sync.enumeration.VersionProviderType.MAVEN_DEPENDENCY
import io.opengood.project.sync.enumeration.VersionProviderType.MAVEN_NEXUS_DEPENDENCY
import io.opengood.project.sync.enumeration.VersionProviderType.MAVEN_WRAPPER
import io.opengood.project.sync.enumeration.VersionSourceType.GRADLE_PLUGINS_REPO
import io.opengood.project.sync.enumeration.VersionSourceType.GRADLE_SERVICES
import io.opengood.project.sync.enumeration.VersionSourceType.MAVEN_CENTRAL_REPO
import io.opengood.project.sync.enumeration.VersionSourceType.NEXUS_HOSTED_REPO
import io.opengood.project.sync.enumeration.VersionSourceType.NEXUS_PROXY_REPO
import io.opengood.project.sync.firstOrDefault
import io.opengood.project.sync.getFileType
import io.opengood.project.sync.getGroupAsPath
import io.opengood.project.sync.getVersionFiles
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import io.opengood.project.sync.model.VersionAttributes
import io.opengood.project.sync.model.VersionChangeData
import io.opengood.project.sync.model.VersionConfigPatterns
import io.opengood.project.sync.model.VersionExclusion
import io.opengood.project.sync.model.VersionGroupAttributes
import io.opengood.project.sync.model.VersionLineData
import io.opengood.project.sync.model.VersionMasterConfig
import io.opengood.project.sync.model.VersionNumberAttributes
import io.opengood.project.sync.model.VersionPattern
import io.opengood.project.sync.model.VersionProjectConfig
import io.opengood.project.sync.model.VersionProvider
import io.opengood.project.sync.model.VersionUri
import io.opengood.project.sync.padSpaces
import io.opengood.project.sync.toDelimiter
import org.apache.commons.lang3.StringUtils
import org.dom4j.DocumentHelper
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.util.regex.Pattern

open class SyncVersions : BaseTask() {

    @Input
    lateinit var workspacePath: String

    @Input
    lateinit var projectPath: String

    init {
        group = "sync"
        description = "Syncs versions for each project"
    }

    @TaskAction
    fun run() {
        execute(
            taskName = TASK_NAME,
            displayName = TASK_DISPLAY_NAME,
            workspacePath = workspacePath,
            projectPath = projectPath
        ) { _, master: SyncMaster, project: SyncProject, _ ->
            val versionFiles = getVersionFiles(project.dir)
            versionFiles.forEach { versionFile ->
                var priorLine = StringUtils.EMPTY
                var prevLine = StringUtils.EMPTY
                Files.write(
                    versionFile.toPath(),
                    Files.lines(versionFile.toPath())
                        .map { line ->
                            var currentLine = line

                            with(master.versions) {
                                providers.forEach { provider ->
                                    if (provider.enabled) {
                                        val data = getVersionChangeData(
                                            versionFile,
                                            master.versions,
                                            project.versions,
                                            provider,
                                            currentLine,
                                            prevLine,
                                            priorLine
                                        )
                                        with(provider) {
                                            if (files.contains(data.file)) {
                                                currentLine = changeLine(data)
                                            }
                                        }
                                    }
                                }
                            }

                            priorLine = prevLine
                            prevLine = currentLine
                            currentLine
                        }
                        .toList()
                )
            }
        }
    }

    private fun changeLine(data: VersionChangeData): String {
        with(data) {
            with(provider) {
                with(line) {
                    with(attributes) {
                        return when {
                            tools.containsAny(GRADLE, MAVEN) -> {
                                when {
                                    files.containsAny(
                                        BUILD_GRADLE_GROOVY,
                                        BUILD_GRADLE_KOTLIN,
                                        SETTINGS_GRADLE_GROOVY,
                                        SETTINGS_GRADLE_KOTLIN
                                    ) -> {
                                        with(group) {
                                            with(version) {
                                                if (key.isNotBlank() && group.isNotBlank() && name.isNotBlank() &&
                                                    current.isNotBlank() && current != patterns.versionPlaceholder
                                                ) {
                                                    if (!isVersionNumberDev(current, patterns)) {
                                                        printInfo("Determining if Gradle dependency/plugin '$group:$name' needs to be updated in file '$file'")
                                                        new = getVersionNumber(data)
                                                        if (StringUtils.isNotBlank(new) && current != new) {
                                                            printProgress("Updating Gradle dependency/plugin '$group:$name' from version '$current' to '$new'...")
                                                            val formattedLine = formatLine(data)
                                                            printDone()
                                                            return formattedLine
                                                        } else {
                                                            printSuccess("No updates required")
                                                            printBlankLine()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        currentLine
                                    }

                                    files.containsAny(MAVEN_POM, VERSIONS_PROPERTIES) -> {
                                        with(group) {
                                            with(version) {
                                                if (key.isNotBlank() && group.isNotBlank() && name.isNotBlank() && current.isNotBlank()) {
                                                    if (!isVersionNumberDev(current, patterns)) {
                                                        printInfo("Determining if Gradle/Maven dependency/plugin '$group:$name' needs to be updated in file '$file'")
                                                        new = getVersionNumber(data)
                                                        if (StringUtils.isNotBlank(new) && current != new) {
                                                            printProgress("Updating Gradle/Maven dependency/plugin '$group:$name' from version '$current' to '$new'...")
                                                            val formattedLine = formatLine(data)
                                                            printDone()
                                                            return formattedLine
                                                        } else {
                                                            printSuccess("No updates required")
                                                            printBlankLine()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        currentLine
                                    }

                                    files.containsAny(GRADLE_WRAPPER_PROPERTIES, MAVEN_WRAPPER_PROPERTIES) -> {
                                        with(version) {
                                            if (current.isNotBlank()) {
                                                if (!isVersionNumberDev(current, patterns)) {
                                                    printInfo("Determining if Gradle/Maven wrapper needs to be updated in file '$file'")
                                                    new = getVersionNumber(data)
                                                    if (StringUtils.isNotBlank(new) && current != new) {
                                                        printProgress("Updating Gradle/Maven wrapper from version '$current' to '$new'...")
                                                        val formattedLine = formatLine(data)
                                                        printDone()
                                                        return formattedLine
                                                    } else {
                                                        printSuccess("No updates required")
                                                        printBlankLine()
                                                    }
                                                }
                                            }
                                        }
                                        currentLine
                                    }

                                    else -> currentLine
                                }
                            }

                            else -> currentLine
                        }
                    }
                }
            }
        }
    }

    private fun downloadAttribute(uri: String, pattern: VersionPattern): String {
        with(pattern) {
            val (_, _, result) = uri.httpGet().responseString()
            return when (result) {
                is Result.Success -> {
                    try {
                        findPatternMatch(key, this, result.get())
                    } catch (e: Exception) {
                        printWarning(
                            "Unable to parse attribute from response for: '$key'",
                            e
                        )
                        StringUtils.EMPTY
                    }
                }

                is Result.Failure -> {
                    printWarning(
                        "Unable to retrieve attribute from request URI '$uri' for: '$key'",
                        result.getException()
                    )
                    StringUtils.EMPTY
                }
            }
        }
    }

    private fun downloadVersionNumber(uri: VersionUri, pattern: String, data: VersionChangeData): String {
        with(data) {
            with(provider) {
                with(uri) {
                    printProgress("Attempting to download new version number from '${uri.uri}...")
                    val (_, _, result) = this.uri.httpGet().responseString()
                    return when (result) {
                        is Result.Success -> {
                            when {
                                source.containsAny(GRADLE_SERVICES) -> {
                                    try {
                                        JsonPath.parse(result.get()).read(pattern)
                                    } catch (e: Exception) {
                                        val types = types.toDelimiter()
                                        printWarning(
                                            "Unable to parse version number from response for version provider(s): '$types'",
                                            e
                                        )
                                        StringUtils.EMPTY
                                    }
                                }

                                source.containsAny(GRADLE_PLUGINS_REPO, MAVEN_CENTRAL_REPO, NEXUS_PROXY_REPO) -> {
                                    try {
                                        val document = DocumentHelper.parseText(result.get())
                                        document.selectNodes(pattern)
                                            .filter { node ->
                                                !isVersionNumberExcluded(node.text, exclusions, attributes)
                                            }
                                            .last { node -> isVersionNumberMatch(node.text, patterns) }
                                            .text
                                    } catch (e: Exception) {
                                        val types = types.toDelimiter()
                                        printWarning(
                                            "Unable to parse version number from response for version provider(s): '$types'",
                                            e
                                        )
                                        StringUtils.EMPTY
                                    }
                                }

                                source.containsAny(NEXUS_HOSTED_REPO) -> {
                                    try {
                                        JsonPath.parse(result.get())
                                            .read<List<Map<String, String>>>(pattern)
                                            .map { it["version"] }
                                            .filter { StringUtils.isNotBlank(it) }
                                            .map { it.toString() }
                                            .filter { version ->
                                                !isVersionNumberExcluded(version, exclusions, attributes)
                                            }
                                            .firstOrDefault(
                                                { version -> isSemanticVersionNumberMatch(version, patterns) },
                                                StringUtils.EMPTY
                                            )
                                    } catch (e: Exception) {
                                        val types = types.toDelimiter()
                                        printWarning(
                                            "Unable to parse version number from response for version provider(s): '$types'",
                                            e
                                        )
                                        StringUtils.EMPTY
                                    }
                                }

                                else -> {
                                    printWarning("Version number parsing from response not supported for version provider(s) '$types'")
                                    StringUtils.EMPTY
                                }
                            }
                        }

                        is Result.Failure -> {
                            val types = types.toDelimiter()
                            printWarning(
                                "Unable to retrieve version number from request URI '${uri.uri}' for version provider(s) '$types'",
                                result.getException()
                            )
                            StringUtils.EMPTY
                        }
                    }
                }
            }
        }
    }

    private fun findPatternMatch(key: String, patterns: List<VersionPattern>, value: String): String {
        val pattern = getPattern(key, patterns)
        if (pattern != VersionPattern.EMPTY && value.isNotBlank()) {
            with(pattern) {
                if (this.value.isBlank()) {
                    val matcher = Pattern.compile(this.pattern).matcher(value)
                    if (matcher.find()) {
                        var match = matcher.group(index)
                        if (trim.isNotEmpty()) {
                            trim.forEach {
                                match = match.replace(it, StringUtils.EMPTY)
                            }
                        }
                        if (match.isNotBlank()) {
                            match = match.trim()
                        }
                        return match
                    }
                } else {
                    return this.value
                }
            }
        }
        return StringUtils.EMPTY
    }

    private fun findPatternMatch(key: String, pattern: VersionPattern, value: String) =
        findPatternMatch(key, listOf(pattern), value)

    private fun formatLine(data: VersionChangeData): String {
        with(data) {
            with(provider) {
                with(line) {
                    with(attributes) {
                        if (write.isNotEmpty()) {
                            val builder = StringBuilder()
                            write.forEach {
                                var line = it.pattern

                                with(group) {
                                    with(version) {
                                        val map = mapOf(
                                            "group" to group,
                                            "id" to id,
                                            "key" to key,
                                            "name" to name,
                                            "uri" to uri,
                                            "version" to new
                                        )
                                        map.forEach {
                                            if (line.contains("{${it.key}}")) {
                                                line = line.replace("{${it.key}}", it.value)
                                            }
                                        }
                                    }
                                }

                                line = padSpaces(line, spaces)

                                if (it.newLine) {
                                    builder.appendLine(line)
                                } else {
                                    builder.append(line)
                                }
                            }
                            return builder.toString()
                        }
                        return currentLine
                    }
                }
            }
        }
    }

    private fun getPattern(key: String, patterns: List<VersionPattern>) =
        patterns.firstOrDefault({ it.key == key }, VersionPattern.EMPTY)

    private fun getPatternLine(key: String, data: VersionChangeData): String {
        with(data) {
            with(provider) {
                with(line) {
                    return when {
                        tools.containsAny(MAVEN) -> {
                            when (key) {
                                "group" -> priorLine
                                "name" -> prevLine
                                else -> currentLine
                            }
                        }

                        else -> currentLine
                    }
                }
            }
        }
    }

    private fun getUri(uri: VersionUri, data: VersionChangeData): VersionUri {
        return with(data) {
            with(provider) {
                with(attributes) {
                    with(uri) {
                        when {
                            tools.containsAny(GRADLE, MAVEN) -> {
                                val group = with(group) {
                                    when {
                                        source.containsAny(NEXUS_HOSTED_REPO) -> group
                                        else -> path
                                    }
                                }
                                VersionUri(
                                    uri = this.uri.replace("{group}", group).replace("{name}", name),
                                    source = source,
                                    pattern = pattern,
                                    index = index
                                )
                            }

                            else -> VersionUri.EMPTY
                        }
                    }
                }
            }
        }
    }

    private fun getVersionChangeData(
        file: File,
        master: VersionMasterConfig,
        project: VersionProjectConfig,
        provider: VersionProvider,
        vararg lines: String
    ): VersionChangeData {
        val data = VersionChangeData(
            file = getFileType(file),
            line = VersionLineData(
                currentLine = lines[0],
                spaces = countSpaces(lines[0]),
                prevLine = lines[1],
                priorLine = lines[2]
            ),
            attributes = VersionAttributes.EMPTY,
            exclusions = master.exclusions + project.exclusions,
            patterns = master.config.patterns,
            provider = provider
        )

        with(data) {
            with(provider) {
                with(attributes) {
                    when {
                        tools.containsAny(GRADLE, MAVEN) -> {
                            when {
                                types.containsAny(
                                    GRADLE_DEPENDENCY,
                                    GRADLE_NEXUS_DEPENDENCY,
                                    MAVEN_DEPENDENCY,
                                    MAVEN_NEXUS_DEPENDENCY
                                ) &&
                                    files.containsAny(data.file) &&
                                    files.containsAny(MAVEN_POM, VERSIONS_PROPERTIES) -> {
                                    with(version) {
                                        key = findPatternMatch("key", read, getPatternLine("key", data))
                                        current = findPatternMatch("version", read, getPatternLine("version", data))
                                        if (key.isNotBlank() && current.isNotBlank()) {
                                            with(group) {
                                                group = findPatternMatch("group", read, getPatternLine("group", data))
                                                path = getGroupAsPath(group)
                                            }
                                            name = findPatternMatch("name", read, getPatternLine("name", data))
                                        }
                                    }
                                }

                                types.containsAny(GRADLE_DEPENDENCY, GRADLE_NEXUS_DEPENDENCY) &&
                                    files.containsAny(data.file) &&
                                    files.containsAny(
                                        BUILD_GRADLE_GROOVY,
                                        BUILD_GRADLE_KOTLIN,
                                        SETTINGS_GRADLE_GROOVY,
                                        SETTINGS_GRADLE_KOTLIN
                                    ) -> {
                                    key = findPatternMatch("key", read, getPatternLine("key", data))
                                    if (key.isNotBlank()) {
                                        with(group) {
                                            group = findPatternMatch("group", read, getPatternLine("group", data))
                                            path = getGroupAsPath(group)

                                            val namePattern = getPattern("name", read)
                                            namePattern.trim.add(group)
                                            name = findPatternMatch("name", namePattern, getPatternLine("name", data))

                                            with(version) {
                                                val versionPattern = getPattern("version", read)
                                                versionPattern.trim.addAll(listOf(group, name))
                                                current =
                                                    findPatternMatch("version", versionPattern, getPatternLine("version", data))
                                            }
                                        }
                                    }
                                }

                                types.containsAny(GRADLE_PLUGIN) &&
                                    files.containsAny(data.file) &&
                                    files.containsAny(
                                        BUILD_GRADLE_GROOVY,
                                        BUILD_GRADLE_KOTLIN,
                                        SETTINGS_GRADLE_GROOVY,
                                        SETTINGS_GRADLE_KOTLIN,
                                        VERSIONS_PROPERTIES
                                    ) -> {
                                    with(version) {
                                        key = findPatternMatch("key", read, getPatternLine("key", data))
                                        current = findPatternMatch("version", read, getPatternLine("version", data))
                                        if (key.isNotBlank() && current.isNotBlank() && current != patterns.versionPlaceholder) {
                                            id = findPatternMatch("id", read, getPatternLine("id", data))
                                            uri = findPatternMatch("uri", read, getPatternLine("uri", data)) + id
                                            with(group) {
                                                group = downloadAttribute(uri, getPattern("group", read))
                                                path = getGroupAsPath(group)

                                                val namePattern = getPattern("name", read)
                                                namePattern.trim.add(group)
                                                name = downloadAttribute(uri, namePattern)
                                            }
                                        }
                                    }
                                }

                                types.containsAny(GRADLE_WRAPPER, MAVEN_WRAPPER) &&
                                    files.containsAny(data.file) &&
                                    files.containsAny(GRADLE_WRAPPER_PROPERTIES, MAVEN_WRAPPER_PROPERTIES) -> {
                                    with(version) {
                                        current = findPatternMatch("version", read, getPatternLine("version", data))
                                        if (current.isNotBlank()) {
                                            key = findPatternMatch("key", read, getPatternLine("key", data))
                                            uri = findPatternMatch("uri", read, getPatternLine("uri", data))
                                        }
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
        return data
    }

    private fun getVersionNumber(data: VersionChangeData): String {
        with(data) {
            with(provider) {
                uris.filter { it.enabled }
                    .forEach { uri ->
                        val downloadUri = getUri(uri, data)
                        val versionNumber = downloadVersionNumber(downloadUri, uri.pattern, data)
                        if (versionNumber.isNotBlank()) {
                            return versionNumber
                        }
                    }
            }
        }
        return StringUtils.EMPTY
    }

    private fun isSemanticVersionNumberMatch(versionNumber: String, patterns: VersionConfigPatterns): Boolean =
        patterns.semanticVersion.toRegex().matches(versionNumber) &&
            !patterns.versionNumberIgnore.any { versionNumber.contains(it) }

    private fun isVersionNumberDev(versionNumber: String, patterns: VersionConfigPatterns): Boolean =
        patterns.devVersion.toRegex().matches(versionNumber)

    private fun isVersionNumberExcluded(
        versionNumber: String,
        exclusions: List<VersionExclusion>,
        attributes: VersionAttributes
    ): Boolean {
        return if (exclusions.isEmpty()) {
            false
        } else {
            with(attributes) {
                with(group) {
                    exclusions
                        .filter { it.group == group && it.name == name }
                        .flatMap { it.versions }
                        .any { versionNumber.startsWith(it.removeSuffix("*")) }
                }
            }
        }
    }

    private fun isVersionNumberMatch(versionNumber: String, patterns: VersionConfigPatterns): Boolean =
        patterns.versionNumber.toRegex().matches(versionNumber) &&
            !patterns.versionNumberIgnore.any { versionNumber.contains(it) }

    companion object {
        const val TASK_NAME = "syncVersions"
        const val TASK_DISPLAY_NAME = "Sync Versions"
    }
}
