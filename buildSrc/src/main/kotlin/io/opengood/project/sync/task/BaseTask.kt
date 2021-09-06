package io.opengood.project.sync.task

import io.opengood.project.sync.createContext
import io.opengood.project.sync.getBuildInfo
import io.opengood.project.sync.getSyncMaster
import io.opengood.project.sync.getSyncProjects
import io.opengood.project.sync.model.BuildInfo
import io.opengood.project.sync.model.SyncContext
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import org.gradle.api.DefaultTask
import org.gradle.internal.logging.text.StyledTextOutput.Style
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf
import java.nio.file.Path

open class BaseTask : DefaultTask() {

    private val out = project.serviceOf<StyledTextOutputFactory>().create("colored-output")

    protected fun execute(
        name: String,
        displayName: String,
        workspaceDir: String,
        projectDir: String,
        task: (context: SyncContext, master: SyncMaster, project: SyncProject, buildInfo: BuildInfo) -> Unit
    ) {
        printHeader(name)
        printExecute(name)

        val context = createContext(workspaceDir = workspaceDir)
        printInfo("Sync context...")
        printInfo("Workspace directory: '${context.workspaceDir}'")
        printBlankLine()

        val master = try {
            getSyncMaster(Path.of(projectDir).toFile())
        } catch (e: Exception) {
            printException("Unable to parse sync master file", e)
            printComplete(name)
            SyncMaster.EMPTY
        }

        if (master != SyncMaster.EMPTY) {
            printInfo("Sync master info...")
            with(master) {
                printInfo("Directory: '$dir'")
                printInfo("File: '$file'")
                printInfo("Version: '$version'")
                printBlankLine()
            }

            val projects = try {
                getSyncProjects(context)
            } catch (e: Exception) {
                printException("Unable to parse sync files", e)
                printComplete(name)
                emptyList()
            }

            if (projects.isNotEmpty()) {
                projects.forEach { project ->
                    val buildInfo = try {
                        getBuildInfo(project.dir)
                    } catch (ignored: Exception) {
                        printWarning("Unable to get build info")
                        BuildInfo.EMPTY
                    }

                    with(project) {
                        printInfo("Project info...")
                        printInfo("Name: '$name'")
                        printInfo("Directory: '$dir'")
                        printBlankLine()

                        printInfo("Sync info...")
                        printInfo("File: '$file'")
                        printInfo("Version: '$version'")
                        printBlankLine()

                        if (buildInfo != BuildInfo.EMPTY) {
                            with(buildInfo) {
                                printInfo("Build info...")
                                printInfo("Language: '$language'")
                                printInfo("Build Gradle: '$buildGradle'")
                                printInfo("Settings Gradle: '$settingsGradle'")
                                printBlankLine()
                            }
                        }

                        printInfo("CI info...")
                        with(ci) {
                            printInfo("Provider type: '$provider'")
                            printInfo("Template: '$template'")
                            printBlankLine()
                        }
                    }

                    task.invoke(context, master, project, buildInfo)

                    printSuccess("Completed sync of $displayName for project: ${project.name}")
                    printBlankLine()
                    printDivider()
                }
            }
            printSuccess("Successfully synced $displayName")
            printComplete(name)
        }
    }

    protected fun printBlankLine() =
        println()

    protected fun printComplete(name: String) =
        print(Style.ProgressStatus, true, "Completed $name task!")

    protected fun printDivider() =
        print(Style.Header, true, "----------------------------------------------")

    protected fun printDone() =
        print(Style.Success, true, "Done!")

    protected fun printException(message: String, e: Exception) =
        print(Style.Error, false, "$message:", e.stackTraceToString())

    protected fun printExecute(name: String) =
        print(Style.ProgressStatus, true, "Executing $name task...")

    protected fun printFailure(message: String) =
        print(Style.Failure, false, message)

    protected fun printHeader(name: String) =
        print(
            Style.Header, true,
            "***************************************************",
            "$name task",
            "***************************************************"
        )

    protected fun printInfo(message: String) =
        print(Style.Info, false, message)

    protected fun printProgress(message: String) =
        print(Style.ProgressStatus, false, message)

    protected fun printSuccess(message: String) =
        print(Style.Success, false, message)

    protected fun printWarning(message: String) =
        print(Style.Description, false, message)

    private fun print(style: Style, blankLine: Boolean, vararg messages: String) =
        with(out.style(style)) {
            messages.forEach { println(it) }
            if (blankLine) println()
        }
}
