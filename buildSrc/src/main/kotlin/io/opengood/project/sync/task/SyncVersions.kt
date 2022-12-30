package io.opengood.project.sync.task

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.opengood.project.sync.countSpaces
import io.opengood.project.sync.enumeration.BuildToolType.GRADLE
import io.opengood.project.sync.enumeration.FileType.VERSIONS_PROPERTIES
import io.opengood.project.sync.getVersionFiles
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import io.opengood.project.sync.model.VersionConfigPatterns
import io.opengood.project.sync.model.VersionPattern
import io.opengood.project.sync.model.VersionPatternResult
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
                            val spaces = countSpaces(line)
                            var currentLine = line

                            with(master.versions) {
                                with(config) {
                                    providers.forEach { provider ->
                                        with(provider) {
                                            if (files.contains(versionFile.name)) {
                                                currentLine = changeLine(
                                                    currentLine,
                                                    spaces,
                                                    provider,
                                                    patterns,
                                                    prevLine,
                                                    priorLine
                                                )
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

    private fun changeLine(
        line: String,
        spaces: Int,
        provider: VersionProvider,
        patterns: VersionConfigPatterns,
        vararg lines: String
    ): String {
        with(provider) {
            return when {
                tools.contains(GRADLE) -> {
                    when {
                        files.contains(VERSIONS_PROPERTIES.toString()) -> {
                            val group = findPatternMatch("group", read, line, 1)
                            val name = findPatternMatch("name", read, line, 1)
                            val currentVersion = findPatternMatch("version", read, line, 1)

                            if (group != VersionPatternResult.EMPTY &&
                                name != VersionPatternResult.EMPTY &&
                                currentVersion != VersionPatternResult.EMPTY
                            ) {
                                val newVersion = getVersionNumber(provider, patterns, group, name)
                            }
                            line
                        }

                        else -> line
                    }
                }

                else -> line
            }
        }
    }

    private fun downloadVersionNumber(
        uri: String,
        pattern: String,
        provider: VersionProvider,
        patterns: VersionConfigPatterns
    ): String {
        with(provider) {
            with(patterns) {
                val (_, _, result) = uri.httpGet().responseString()
                return when (result) {
                    is Result.Success -> {
                        when (type) {
                            else -> {
                                try {
                                    val document = DocumentHelper.parseText(result.get())
                                    document.selectNodes(pattern)
                                        .last { node ->
                                            versionNumber.toRegex().matches(node.text) &&
                                                !versionNumberIgnore.any { node.text.contains(it) }
                                        }.text
                                } catch (e: Exception) {
                                    printWarning("Unable to parse version number from response for version provider '$type'", e)
                                    StringUtils.EMPTY
                                }
                            }
                        }
                    }

                    is Result.Failure -> {
                        printWarning("Unable to retrieve version number from request URI '$uri' for version provider '$type'", result.getException())
                        StringUtils.EMPTY
                    }
                }
            }
        }
    }

    private fun findPatternMatch(
        key: String,
        patterns: List<VersionPattern>,
        value: String,
        index: Int
    ): VersionPatternResult {
        val pattern = patterns.first { it.key == key }
        val matcher = Pattern.compile(pattern.pattern).matcher(value)
        if (matcher.find()) {
            return VersionPatternResult(key = key, value = matcher.group(index))
        }
        return VersionPatternResult.EMPTY
    }

    private fun getUri(
        uri: VersionUri,
        provider: VersionProvider,
        vararg params: VersionPatternResult
    ): String {
        return with(provider) {
            with(uri) {
                when {
                    tools.contains(GRADLE) -> {
                        val group = params.getValue("group").asPath()
                        val name = params.getValue("name")
                        this.uri.replace("{group}", group).replace("{name}", name)
                    }

                    else -> StringUtils.EMPTY
                }
            }
        }
    }

    private fun getVersionNumber(
        provider: VersionProvider,
        patterns: VersionConfigPatterns,
        vararg params: VersionPatternResult
    ): String {
        with(provider) {
            uris.forEach { uri ->
                val downloadUri = getUri(uri, provider, *params)
                val versionNumber = downloadVersionNumber(downloadUri, uri.pattern, provider, patterns)
                if (versionNumber.isNotBlank()) {
                    return versionNumber
                }
            }
        }
        return StringUtils.EMPTY
    }

    private fun String.asPath(): String =
        this.replace(".", "/")

    private fun Array<out VersionPatternResult>.getValue(key: String): String =
        this.first { it.key == key }.value

    companion object {
        const val TASK_NAME = "syncVersions"
        const val TASK_DISPLAY_NAME = "Sync Versions"
    }
}
