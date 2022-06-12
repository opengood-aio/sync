package io.opengood.project.sync.task

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.jayway.jsonpath.JsonPath
import io.opengood.project.sync.constant.Patterns
import io.opengood.project.sync.enumeration.VersionFileType
import io.opengood.project.sync.enumeration.VersionType
import io.opengood.project.sync.model.BuildInfo
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import io.opengood.project.sync.model.VersionMaster
import io.opengood.project.sync.countSpaces
import io.opengood.project.sync.padSpaces
import io.opengood.project.sync.settingsGradleType
import org.apache.commons.lang3.StringUtils
import org.dom4j.DocumentHelper
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.util.regex.Pattern
import kotlin.streams.toList

open class SyncVersions : BaseTask() {

    @Input
    lateinit var workspaceDir: String

    @Input
    lateinit var selectedProject: String

    init {
        group = "sync"
        description = "Syncs versions for each project"
    }

    @TaskAction
    fun run() {
        execute(
            name = TASK_NAME,
            displayName = TASK_DISPLAY_NAME,
            workspaceDir = workspaceDir,
            selectedProject = selectedProject,
            projectDir = project.projectDir.absolutePath
        ) { _, master: SyncMaster, project: SyncProject, buildInfo: BuildInfo ->
            val versions = master.versioning
            versions.forEach { version ->
                printInfo("Determining if project name '${project.name}' is contained in version key '${version.key}'...")
                if (project.name != version.id) {
                    printProgress("Project name '${project.name}' is not contained in version key")

                    printInfo("Determining if version type '${version.type}' applies...")
                    if (version.tools.contains(buildInfo.buildTool)) {
                        printProgress("Version type '${version.type}' applicable")

                        version.files.forEach { file ->
                            printInfo("Determining if version file '${file}' exists...")
                            val versionFile = getVersionFile(project, file)
                            if (versionFile.exists()) {
                                printInfo("Version file '${file}' exists")

                                printInfo("Determining if version key '${version.key}' exists in file...")
                                if (versionFile.readText(Charsets.UTF_8).contains(version.key)) {
                                    printProgress("Version key '${version.key}' exists in file")

                                    printInfo("Determining version number for '${version.name}'")
                                    val versionNumber = try {
                                        when (version.type) {
                                            VersionType.DOCKER_IMAGE -> {
                                                getVersion(version, Patterns.DOCKER_IMAGE)
                                            }
                                            VersionType.GRADLE_WRAPPER -> {
                                                getVersion(version, Patterns.GRADLE_WRAPPER)
                                            }
                                            VersionType.GRADLE_NEXUS_DEPENDENCY,
                                            VersionType.MAVEN_NEXUS_DEPENDENCY -> {
                                                getVersion(version, Patterns.NEXUS_DEPENDENCY)
                                            }
                                            else -> {
                                                getVersion(version, Patterns.MAVEN_DEPENDENCY)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        printException("Unable to get version number", e)
                                        "None"
                                    }

                                    if (versionNumber != "None") {
                                        printInfo("Found version number: $versionNumber")

                                        val inclusions = getVersionInclusions(version, project)
                                        if (isVersionIncluded(versionNumber, inclusions)) {
                                            printProgress("Updating version number to $versionNumber")
                                            writeVersion(version, versionFile, versionNumber)
                                            printProgress("Version number updated to $versionNumber")
                                        } else {
                                            printWarning(
                                                "Version number '$versionNumber' is excluded from inclusions list: ${
                                                    StringUtils.join(
                                                        inclusions,
                                                        ","
                                                    )
                                                }"
                                            )
                                        }
                                    }
                                } else {
                                    printWarning("Version key '${version.key}' does not exist in file. Unable to write version number to file.")
                                }
                            } else {
                                printWarning("Version file '$versionFile' does not exist. Unable to write version number to file.")
                            }
                        }
                        printDone()
                    } else {
                        printInfo("Version type '${version.type}' not applicable. Skipping.")
                        printDone()
                    }
                } else {
                    printInfo("Project name '${project.name}' is contained in version key. Skipping.")
                    printDone()
                }
            }
        }
    }

    private fun getVersion(version: VersionMaster, pattern: Pattern): String {
        val (_, _, result) = version.uri.httpGet().responseString()
        return when (result) {
            is Result.Success -> {
                when (version.type) {
                    VersionType.DOCKER_IMAGE -> {
                        JsonPath.parse(result.get()).read<List<String>>(pattern.pattern()).last()
                    }
                    VersionType.GRADLE_WRAPPER -> {
                        JsonPath.parse(result.get()).read(pattern.pattern())
                    }
                    VersionType.GRADLE_NEXUS_DEPENDENCY,
                    VersionType.MAVEN_NEXUS_DEPENDENCY -> {
                        try {
                            JsonPath.parse(result.get()).read<List<Map<String, String>>>(pattern.pattern())
                                .first {
                                    Patterns.SEMANTIC_VERSION.toRegex().matches(it["version"].toString()) &&
                                        !Patterns.VERSION_NUMBER_IGNORE.contains(it["version"].toString())
                                }["version"].toString()
                        } catch (e: Exception) {
                            printWarning("Unable to retrieve version: ${e.message}")
                            ""
                        }
                    }
                    else -> {
                        try {
                            val document = DocumentHelper.parseText(result.get())
                            document.selectNodes(pattern.pattern())
                                .last { node ->
                                    Patterns.VERSION_NUMBER.toRegex().matches(node.text) &&
                                        !Patterns.VERSION_NUMBER_IGNORE.any { node.text.contains(it) }
                                }.text
                        } catch (e: Exception) {
                            printWarning("Unable to retrieve version: ${e.message}")
                            ""
                        }
                    }
                }
            }
            is Result.Failure -> {
                printWarning("Unable to retrieve version from '${version.uri}': ${result.getException().message}")
                ""
            }
        }
    }

    private fun getVersionFile(project: SyncProject, versionFile: VersionFileType): File {
        with(project) {
            return when (versionFile) {
                VersionFileType.BUILD_GRADLE,
                VersionFileType.SETTINGS_GRADLE -> {
                    File(dir, settingsGradleType(dir).toString())
                }
                else -> {
                    File(dir, versionFile.toString())
                }
            }
        }
    }

    private fun getVersionInclusions(version: VersionMaster, project: SyncProject): List<String> =
        version.inclusions + project.versioning.filter { it.id == version.id }.flatMap { it.inclusions }

    private fun isVersionIncluded(versionNumber: String, inclusions: List<String>): Boolean {
        return if (inclusions.isEmpty()) {
            true
        } else {
            inclusions.any { versionNumber.startsWith(it.removeSuffix("*")) }
        }
    }

    private fun writeVersion(version: VersionMaster, file: File, versionNumber: String) {
        with(file) {
            with(version) {
                var prevLine = StringUtils.EMPTY
                val lines = Files.lines(toPath())
                    .map { line ->
                        val spaces = countSpaces(line)
                        val currentLine = when {
                            StringUtils.isBlank(subKey) && line.contains(key) ->
                                padSpaces(
                                    String.format(
                                        pattern,
                                        key,
                                        versionNumber
                                    ), spaces
                                )
                            StringUtils.isNotBlank(subKey) && prevLine.contains(key) && line.contains(subKey) ->
                                padSpaces(
                                    String.format(
                                        pattern,
                                        versionNumber
                                    ), spaces
                                )
                            else -> line
                        }
                        prevLine = currentLine
                        currentLine
                    }
                    .toList()
                Files.write(toPath(), lines)
            }
        }
    }

    companion object {
        const val TASK_NAME = "syncVersions"
        const val TASK_DISPLAY_NAME = "Sync Versions"
    }
}
