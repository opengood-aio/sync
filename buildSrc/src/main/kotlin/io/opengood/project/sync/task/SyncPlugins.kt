package io.opengood.project.sync.task

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.opengood.project.sync.enumeration.PluginType
import io.opengood.project.sync.enumeration.VersionsFileType
import io.opengood.project.sync.model.BuildInfo
import io.opengood.project.sync.model.Plugin
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import io.opengood.project.sync.settingsGradleType
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import kotlin.streams.toList

open class SyncPlugins : BaseTask() {

    @Input
    lateinit var workspaceDir: String

    init {
        group = "sync"
        description = "Syncs plugin versions for each project"
    }

    @TaskAction
    fun run() {
        execute(
            name = TASK_NAME,
            displayName = TASK_DISPLAY_NAME,
            workspaceDir = workspaceDir,
            projectDir = project.projectDir.absolutePath
        ) { _, master: SyncMaster, project: SyncProject, buildInfo: BuildInfo ->
            val plugins = master.plugins
            plugins.forEach { plugin ->
                printInfo("Determining version for plugin '${plugin.name}' of type '${plugin.type}'")

                val version = when {
                    plugin.type == PluginType.GRADLE && plugin.file == VersionsFileType.VERSION_PROPERTIES ||
                        plugin.file == VersionsFileType.SETTINGS_GRADLE -> {
                        getGradlePluginVersion(plugin)
                    }
                    else -> {
                        throw RuntimeException("Unable to determine plugin version")
                    }
                }

                printInfo("Found plugin version: $version")
                printProgress("Updating plugin version to $version")
                writePluginVersion(project, plugin, version)
                printDone()
            }
        }
    }

    private fun getGradlePluginVersion(plugin: Plugin): String {
        val (_, _, result) = plugin.uri.httpGet().responseString()
        return when (result) {
            is Result.Success -> {
                val data: String = result.get()

                val versionStringRegex = """\<h3\>Version.*\(latest\).*\<\/h3\>""".toRegex()
                val versionStringMatch = versionStringRegex.find(data)?.value!!

                val versionRegex = """(?:(\d+)\.)?(?:(\d+)\.)?(?:(\d+)\.\d+)""".toRegex()
                val versionMatch = versionRegex.find(versionStringMatch)
                versionMatch?.value ?: throw RuntimeException("Unable to parse plugin version")
            }
            is Result.Failure -> {
                throw RuntimeException(
                    "Unable to retrieve plugin version from '${plugin.uri}",
                    result.getException()
                )
            }
        }
    }

    private fun getVersionFormat(versionsFile: VersionsFileType, key: String, version: String): String {
        return when (versionsFile) {
            VersionsFileType.VERSION_PROPERTIES -> {
                "$key=$version"
            }
            VersionsFileType.SETTINGS_GRADLE -> {
                "    id(\"$key\") version \"$version\""
            }
        }
    }

    private fun getVersionsFile(project: SyncProject, versionsFile: VersionsFileType): File {
        with(project) {
            return when (versionsFile) {
                VersionsFileType.VERSION_PROPERTIES -> {
                    File(dir, versionsFile.toString())
                }
                VersionsFileType.SETTINGS_GRADLE -> {
                    File(dir, settingsGradleType(dir).toString())
                }
            }
        }
    }

    private fun writePluginVersion(project: SyncProject, plugin: Plugin, version: String) {
        val file = getVersionsFile(project, plugin.file)
        with(file) {
            with(plugin) {
                Files.write(
                    toPath(),
                    Files.lines(toPath())
                        .map { line ->
                            when {
                                line.contains(key) -> getVersionFormat(plugin.file, key, version)
                                else -> line
                            }
                        }
                        .toList()
                )
            }
        }
    }

    companion object {
        const val TASK_NAME = "syncPlugins"
        const val TASK_DISPLAY_NAME = "Plugins"
    }
}
