package io.opengood.project.sync.task

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.jayway.jsonpath.JsonPath
import io.opengood.project.sync.containsAny
import io.opengood.project.sync.countSpaces
import io.opengood.project.sync.enumeration.BuildToolType.GRADLE
import io.opengood.project.sync.enumeration.BuildToolType.MAVEN
import io.opengood.project.sync.enumeration.FileType.MAVEN_POM
import io.opengood.project.sync.enumeration.FileType.VERSIONS_PROPERTIES
import io.opengood.project.sync.enumeration.VersionSourceType.MAVEN_CENTRAL
import io.opengood.project.sync.enumeration.VersionSourceType.NEXUS_HOSTED_REPO
import io.opengood.project.sync.enumeration.VersionSourceType.NEXUS_PROXY_REPO
import io.opengood.project.sync.getGroupAsPath
import io.opengood.project.sync.getVersionFiles
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import io.opengood.project.sync.model.VersionAttributes
import io.opengood.project.sync.model.VersionChangeData
import io.opengood.project.sync.model.VersionConfigPatterns
import io.opengood.project.sync.model.VersionExclusion
import io.opengood.project.sync.model.VersionLineData
import io.opengood.project.sync.model.VersionMasterConfig
import io.opengood.project.sync.model.VersionPattern
import io.opengood.project.sync.model.VersionProjectConfig
import io.opengood.project.sync.model.VersionProvider
import io.opengood.project.sync.model.VersionUri
import io.opengood.project.sync.padSpaces
import io.opengood.project.sync.toDelimiter
import io.opengood.project.sync.toEnum
import org.apache.commons.lang3.StringUtils
import org.dom4j.DocumentHelper
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
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
                                            master.versions,
                                            project.versions,
                                            provider,
                                            currentLine,
                                            prevLine,
                                            priorLine
                                        )
                                        with(provider) {
                                            if (files.contains(versionFile.name.toEnum())) {
                                                currentLine = changeLine(data)
                                            }
                                        }
                                    } else {
                                        printInfo("Version provider '${provider.name}' disabled. Skipping...")
                                        printBlankLine()
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
                    return when {
                        tools.containsAny(GRADLE, MAVEN) -> {
                            when {
                                files.containsAny(MAVEN_POM, VERSIONS_PROPERTIES) -> {
                                    with(attributes) {
                                        group = findPatternMatch("group", read, getPatternLine("group", data))
                                        groupPath = getGroupAsPath(group)
                                        name = findPatternMatch("name", read, getPatternLine("name", data))
                                        currentVersion =
                                            findPatternMatch("version", read, getPatternLine("version", data))

                                        if (group.isNotBlank() && name.isNotBlank() && currentVersion.isNotBlank()) {
                                            if (!isVersionNumberDev(currentVersion, patterns)) {
                                                newVersion = getVersionNumber(data)
                                                if (StringUtils.isNotBlank(newVersion) && currentVersion != newVersion) {
                                                    return formatLine(data)
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

    private fun downloadVersionNumber(uri: VersionUri, pattern: String, data: VersionChangeData): String {
        with(data) {
            with(provider) {
                with(uri) {
                    val (_, _, result) = this.uri.httpGet().responseString()
                    return when (result) {
                        is Result.Success -> {
                            when {
                                source.containsAny(MAVEN_CENTRAL, NEXUS_PROXY_REPO) -> {
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
                                            .first { version -> isSemanticVersionNumberMatch(version, patterns) }
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
                                "Unable to retrieve version number from request URI '$uri' for version provider(s) '$types'",
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
        val pattern = patterns.first { it.key == key }
        with(pattern) {
            val matcher = Pattern.compile(this.pattern).matcher(value)
            if (matcher.find()) {
                var match = matcher.group(index)
                if (trim.isNotEmpty()) {
                    trim.forEach {
                        match = match.replace(it, StringUtils.EMPTY)
                    }
                }
                return match
            }
        }
        return StringUtils.EMPTY
    }

    private fun formatLine(data: VersionChangeData): String {
        with(data) {
            with(provider) {
                with(attributes) {
                    with(line) {
                        if (write.isNotEmpty()) {
                            val builder = StringBuilder()
                            write.forEach {
                                var line = it.pattern

                                val map = mapOf(
                                    "group" to group,
                                    "name" to name,
                                    "version" to newVersion
                                )
                                map.forEach {
                                    if (line.contains("{${it.key}}")) {
                                        line = line.replace("{${it.key}}", it.value)
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

    private fun getGroupAsUriParameter(uri: VersionUri, data: VersionChangeData): String {
        with(data) {
            with(uri) {
                with(attributes) {
                    return when {
                        source.containsAny(NEXUS_HOSTED_REPO) -> group

                        else -> groupPath
                    }
                }
            }
        }
    }

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
                                val group = getGroupAsUriParameter(uri, data)
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
        master: VersionMasterConfig,
        project: VersionProjectConfig,
        provider: VersionProvider,
        vararg lines: String
    ) =
        VersionChangeData(
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

    private fun getVersionNumber(data: VersionChangeData): String {
        with(data) {
            with(provider) {
                uris.forEach { uri ->
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
                exclusions
                    .filter { it.group == group && it.name == name }
                    .flatMap { it.versions }
                    .any { versionNumber.startsWith(it.removeSuffix("*")) }
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
