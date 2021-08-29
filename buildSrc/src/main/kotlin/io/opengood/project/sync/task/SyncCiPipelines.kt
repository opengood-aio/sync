package io.opengood.project.sync.task

import io.opengood.project.sync.getCiProvider
import io.opengood.project.sync.model.SyncContext
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import io.opengood.project.sync.then
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileNotFoundException

open class SyncCiPipelines : BaseTask() {

    @Input
    lateinit var workspaceDir: String

    init {
        group = "sync"
        description = "Syncs CI pipelines from template source for each project"
    }

    @TaskAction
    fun run() {
        execute(
            name = TASK_NAME,
            displayName = TASK_DISPLAY_NAME,
            workspaceDir = workspaceDir,
            projectDir = project.projectDir.absolutePath
        ) { context: SyncContext, master: SyncMaster, project: SyncProject ->
            val provider = master.getCiProvider(project.ci.provider)
            with(provider.template) {
                val srcDir = File("${context.workspaceDir}/${src.repo}/${src.path}/${project.ci.template}")
                (!srcDir.exists()) then { throw FileNotFoundException("CI pipeline templates source directory cannot be found: $srcDir") }

                val targetDir = File("${project.dir}/${target.path}")
                (!targetDir.exists()) then { throw FileNotFoundException("CI pipeline target directory cannot be found: $targetDir") }

                printInfo("Copying CI provider '${project.ci.provider}' pipeline templates from source directory: '$srcDir'...")
                printBlankLine()

                srcDir.walkTopDown()
                    .filter { !it.isDirectory }
                    .forEach { file ->
                        printProgress("Copying CI provider pipeline template '${file.name}' to target directory: '$targetDir'")
                        file.copyTo(File("$targetDir/${file.name}"), true)
                        printDone()
                    }
            }
        }
    }

    companion object {
        const val TASK_NAME = "syncCiPipelines"
        const val TASK_DISPLAY_NAME = "CI Pipelines"
    }
}
