package io.opengood.project.sync.task

import io.opengood.project.sync.enumeration.CiProviderType
import io.opengood.project.sync.getPathAsFile
import io.opengood.project.sync.model.CiProjectConfig
import io.opengood.project.sync.model.CiProvider
import io.opengood.project.sync.model.SyncContext
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.FileNotFoundException

open class SyncCiTemplates : BaseTask() {

    @Input
    lateinit var workspacePath: String

    @Input
    lateinit var projectPath: String

    init {
        group = "sync"
        description = "Syncs CI source templates to target pipelines directory for each project"
    }

    @TaskAction
    fun run() {
        execute(
            taskName = TASK_NAME,
            displayName = TASK_DISPLAY_NAME,
            workspacePath = workspacePath,
            projectPath = projectPath
        ) { context: SyncContext, master: SyncMaster, project: SyncProject, _ ->
            if (project.ci != CiProjectConfig.EMPTY) {
                val provider = master.getCiProvider(project.ci.provider)
                with(provider.template) {
                    val srcDir =
                        getPathAsFile(context.workspaceDir.absolutePath, src.repo, src.path, project.ci.template)
                    if (!srcDir.exists()) {
                        throw FileNotFoundException("CI templates source directory cannot be found: $srcDir")
                    }

                    val targetDir = getPathAsFile(project.dir.absolutePath, target.path)
                    if (!targetDir.exists()) {
                        throw FileNotFoundException("CI pipelines target directory cannot be found: $targetDir")
                    }

                    printInfo("Copying CI provider '${project.ci.provider}' templates from source directory: '$srcDir'...")
                    printBlankLine()

                    srcDir.walkTopDown()
                        .filter { !it.isDirectory }
                        .forEach { file ->
                            printProgress("Copying CI provider template '${file.name}' to target directory: '$targetDir'")
                            file.copyTo(getPathAsFile(targetDir.absolutePath, file.name), true)
                            printDone()
                        }
                }
            }
        }
    }

    private fun SyncMaster.getCiProvider(provider: CiProviderType): CiProvider =
        ci.providers.first { it.name == provider }

    companion object {
        const val TASK_NAME = "syncCiTemplates"
        const val TASK_DISPLAY_NAME = "Sync CI Templates"
    }
}
