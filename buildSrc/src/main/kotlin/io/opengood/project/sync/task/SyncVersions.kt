package io.opengood.project.sync.task

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.opengood.project.sync.countSpaces
import io.opengood.project.sync.enumeration.BuildToolType.GRADLE
import io.opengood.project.sync.enumeration.FileType.VERSIONS_PROPERTIES
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
                                    val data = getVersionChangeData(
                                        master.versions,
                                        project.versions,
                                        provider,
                                        currentLine,
                                        prevLine,
                                        priorLine
                                    )
                                    with(provider) {
                                        if (files.contains(versionFile.name)) {
                                            currentLine = changeLine(data)
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
                    return when {
                        tools.contains(GRADLE) -> {
                            when {
                                files.contains(VERSIONS_PROPERTIES.toString()) -> {
                                    with(attributes) {
                                        group = findPatternMatch("group", read, currentLine)
                                        name = findPatternMatch("name", read, currentLine)
                                        currentVersion = findPatternMatch("version", read, currentLine)

                                        if (group.isNotBlank() && name.isNotBlank() && currentVersion.isNotBlank()) {
                                            if (!isVersionNumberDev(currentVersion, patterns)) {
                                                newVersion = getVersionNumber(data)
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

    private fun downloadVersionNumber(uri: String, pattern: String, data: VersionChangeData): String {
        with(data) {
            with(provider) {
                val (_, _, result) = uri.httpGet().responseString()
                return when (result) {
                    is Result.Success -> {
                        when (type) {
                            else -> {
                                try {
                                    val document = DocumentHelper.parseText(result.get())
                                    document.selectNodes(pattern)
                                        .filter { node ->
                                            !isVersionNumberExcluded(node.text, exclusions, attributes)
                                        }
                                        .last { node -> isVersionNumberMatch(node.text, patterns) }
                                        .text
                                } catch (e: Exception) {
                                    printWarning(
                                        "Unable to parse version number from response for version provider '$type'",
                                        e
                                    )
                                    StringUtils.EMPTY
                                }
                            }
                        }
                    }

                    is Result.Failure -> {
                        printWarning(
                            "Unable to retrieve version number from request URI '$uri' for version provider '$type'",
                            result.getException()
                        )
                        StringUtils.EMPTY
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

    private fun getUri(uri: VersionUri, data: VersionChangeData): String {
        return with (data) {
            with(provider) {
                with(attributes) {
                    with(uri) {
                        when {
                            tools.contains(GRADLE) -> {
                                val group = getGroupAsPath(group)
                                val name = name
                                this.uri.replace("{group}", group).replace("{name}", name)
                            }

                            else -> StringUtils.EMPTY
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

    private fun isVersionNumberDev(versionNumber: String, patterns: VersionConfigPatterns): Boolean =
        patterns.devVersion.toRegex().matches(versionNumber)

    private fun isVersionNumberExcluded(
        versionNumber: String,
        exclusions: List<VersionExclusion>,
        attributes: VersionAttributes
    ): Boolean {
        return if (exclusions.isEmpty()) {
            true
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
