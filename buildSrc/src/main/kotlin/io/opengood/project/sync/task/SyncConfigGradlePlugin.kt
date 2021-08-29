package io.opengood.project.sync.task

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.opengood.project.sync.constant.Endpoints
import io.opengood.project.sync.constant.VersionKeys
import io.opengood.project.sync.model.SyncProject
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import kotlin.streams.toList

open class SyncConfigGradlePlugin : BaseTask() {

    @Input
    lateinit var workspaceDir: String

    init {
        group = "sync"
        description = "Syncs OpenGood Config Gradle plugin version for each project"
    }

    @TaskAction
    fun run() {
        val (_, _, result) = Endpoints.OPENGOOD_CONFIG_PLUGIN.httpGet().responseString()

        val version = when (result) {
            is Result.Failure -> {
                throw RuntimeException(
                    "Unable to retrieve OpenGood Config plugin version from '${Endpoints.OPENGOOD_CONFIG_PLUGIN}",
                    result.getException()
                )
            }
            is Result.Success -> {
                val data: String = result.get()

                val versionStringRegex = """\<h3\>Version.*\(latest\).*\<\/h3\>""".toRegex()
                val versionStringMatch = versionStringRegex.find(data)?.value!!

                val versionRegex = """(?:(\d+)\.)?(?:(\d+)\.)?(?:(\d+)\.\d+)""".toRegex()
                val versionMatch = versionRegex.find(versionStringMatch)

                printInfo("Located OpenGood Config plugin version: ${versionMatch?.value}")
                printBlankLine()

                versionMatch?.value
            }
        }

        execute(
            name = TASK_NAME,
            displayName = TASK_DISPLAY_NAME,
            workspaceDir = workspaceDir,
            projectDir = project.projectDir.absolutePath
        ) { _, _, project: SyncProject ->
            with(project.versions) {
                Files.write(
                    toPath(),
                    Files.lines(toPath())
                        .map { line ->
                            when {
                                line.contains(VersionKeys.OPENGOOD_CONFIG_PLUGIN) -> "${VersionKeys.OPENGOOD_CONFIG_PLUGIN}=$version"
                                else -> line
                            }
                        }
                        .toList()
                )
            }
        }
    }

    companion object {
        const val TASK_NAME = "syncConfigGradlePlugin"
        const val TASK_DISPLAY_NAME = "Config Gradle Plugin"
    }
}
