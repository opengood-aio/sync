package io.opengood.project.sync.task

import io.opengood.project.sync.createContext
import io.opengood.project.sync.getCiProvider
import io.opengood.project.sync.getSyncMaster
import io.opengood.project.sync.getSyncProjects
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
        printHeader(TASK_NAME)
        printExecute(TASK_NAME)

        val context = createContext(workspaceDir = workspaceDir)
        printInfo("Sync context...")
        printInfo("Workspace directory: '${context.workspaceDir}'")
        printBlankLine()

        val master = getSyncMaster(project.projectDir)
        printInfo("Sync master info...")
        with(master) {
            with(metadata) {
                printInfo("Directory: '$dir'")
                printInfo("File: '$syncFile'")
                printInfo("Version: '$version'")
                printBlankLine()
            }
        }

        val projects = getSyncProjects(context)
        projects.forEach { project ->
            with(project) {
                with(metadata) {
                    printInfo("Project info...")
                    printInfo("Name: '$name'")
                    printInfo("Directory: '$dir'")
                    printBlankLine()

                    printInfo("Sync info...")
                    printInfo("File: '$syncFile'")
                    printInfo("Version: '$version'")
                    printBlankLine()
                }

                printInfo("CI info...")
                with(ci) {
                    printInfo("Provider type: '$providerType'")
                    printInfo("Template: '$template'")
                    printBlankLine()
                }
            }

            val ciProvider = master.getCiProvider(project.ci.providerType)
            with(ciProvider.template) {
                val srcDir = File("${context.workspaceDir}/${src.repo}/${src.path}/${project.ci.template}")
                (!srcDir.exists()) then { throw FileNotFoundException("CI pipeline templates source directory cannot be found: $srcDir") }

                val targetDir = File("${project.metadata.dir}/${target.path}")
                (!targetDir.exists()) then { throw FileNotFoundException("CI pipeline templates target directory cannot be found: $targetDir") }

                printInfo("Copying CI provider '${project.ci.providerType}' pipeline templates from source directory: '$srcDir'...")
                printBlankLine()

                srcDir.walkTopDown()
                    .filter { !it.isDirectory }
                    .forEach { file ->
                        printProgress("Copying CI provider pipeline template '${file.name}' to target directory: '$targetDir'")
                        file.copyTo(File("$targetDir/${file.name}"), true)
                        printDone()
                    }
            }

            printSuccess("Completed sync of CI pipelines for project: ${project.name}")
            printBlankLine()
            printBlankLine()
        }
        printSuccess("Successfully synced CI pipelines")
        printComplete(TASK_NAME)
    }

    companion object {
        const val TASK_NAME = "syncCiPipelines"
    }
}
