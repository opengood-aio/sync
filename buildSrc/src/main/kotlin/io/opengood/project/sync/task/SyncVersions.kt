package io.opengood.project.sync.task

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.opengood.project.sync.constant.RegExs
import io.opengood.project.sync.enumeration.VersioningType
import io.opengood.project.sync.enumeration.VersionsFileType
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import io.opengood.project.sync.model.Version
import io.opengood.project.sync.settingsGradleType
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.util.regex.Pattern
import kotlin.streams.toList

open class SyncVersions : BaseTask() {

    @Input
    lateinit var workspaceDir: String

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
            projectDir = project.projectDir.absolutePath
        ) { _, master: SyncMaster, project: SyncProject, _ ->
            val versions = master.versioning
            versions.forEach { version ->
                printInfo("Determining version for '${version.name}' of type '${version.type}'")

                val versionNumber = when {
                    version.type == VersioningType.GRADLE_PLUGIN && version.file == VersionsFileType.VERSION_PROPERTIES ||
                        version.file == VersionsFileType.SETTINGS_GRADLE -> {
                        getVersion(version, RegExs.GRADLE_PLUGIN)
                    }
                    version.type == VersioningType.GRADLE_WRAPPER -> {
                        getVersion(version, RegExs.GRADLE_WRAPPER)
                    }
                    else -> {
                        throw RuntimeException("Unable to determine version")
                    }
                }

                printInfo("Found version: $versionNumber")
                printProgress("Updating version to $versionNumber")
                val versionFile = getVersionsFile(project, version.file)
                if (versionFile.exists())
                    writeVersion(project, version, versionFile, versionNumber)
                else
                    printWarning("Unable to write version file as it does not exist: '$versionFile'")
                printDone()
            }
        }
    }

    private fun getVersion(version: Version, locator: Pattern): String {
        val (_, _, result) = version.uri.httpGet().responseString()
        return when (result) {
            is Result.Success -> {
                val versionStringMatch = locator.toRegex().find(result.get())
                if (versionStringMatch?.value != null) {
                    val versionMatch = RegExs.VERSION_NUMBER.toRegex().find(versionStringMatch.value)
                    versionMatch?.value ?: throw RuntimeException("Unable to parse version")
                } else {
                    throw RuntimeException("Unable to parse version")
                }
            }
            is Result.Failure -> {
                throw RuntimeException(
                    "Unable to retrieve version from '${version.uri}",
                    result.getException()
                )
            }
        }
    }

    private fun getVersionsFile(project: SyncProject, versionsFile: VersionsFileType): File {
        with(project) {
            return when (versionsFile) {
                VersionsFileType.GRADLE_WRAPPER_PROPERTIES, VersionsFileType.VERSION_PROPERTIES -> {
                    File(dir, versionsFile.toString())
                }
                VersionsFileType.SETTINGS_GRADLE -> {
                    File(dir, settingsGradleType(dir).toString())
                }
            }
        }
    }

    private fun writeVersion(project: SyncProject, version: Version, file: File, versionNumber: String) {
        with(file) {
            with(version) {
                Files.write(
                    toPath(),
                    Files.lines(toPath())
                        .map { line ->
                            when {
                                line.contains(key) -> String.format(pattern, key, versionNumber)
                                else -> line
                            }
                        }
                        .toList()
                )
            }
        }
    }

    companion object {
        const val TASK_NAME = "syncVersions"
        const val TASK_DISPLAY_NAME = "Versions"
    }
}
