package io.opengood.project.sync.task

import io.opengood.project.sync.createContext
import io.opengood.project.sync.getBuildInfo
import io.opengood.project.sync.getPathAsFile
import io.opengood.project.sync.getSyncMaster
import io.opengood.project.sync.getSyncProjects
import io.opengood.project.sync.model.BuildInfo
import io.opengood.project.sync.model.ConfigInfo
import io.opengood.project.sync.model.GitInfo
import io.opengood.project.sync.model.SyncContext
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import io.opengood.project.sync.model.VersionMasterConfig
import io.opengood.project.sync.model.VersionProvider
import org.gradle.api.DefaultTask
import org.gradle.internal.logging.text.StyledTextOutput.Style
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf

open class BaseTask : DefaultTask() {

    private val out = project.serviceOf<StyledTextOutputFactory>().create("colored-output")

    protected fun execute(
        taskName: String,
        displayName: String,
        workspacePath: String,
        projectPath: String,
        task: (context: SyncContext, master: SyncMaster, project: SyncProject, buildInfo: BuildInfo) -> Unit,
    ) {
        printHeader(taskName)
        printExecute(displayName)

        val context = createContext(workspacePath = workspacePath, syncProjectPath = project.projectDir.absolutePath)
        printInfo("Sync context...")
        printInfo("Workspace directory: '${context.workspaceDir}'")
        printInfo("Sync project directory: '${context.syncProjectDir}'")
        printBlankLine()

        val master = try {
            getSyncMaster(context.syncProjectDir)
        } catch (e: Exception) {
            printException("Unable to parse sync master file", e)
            printComplete(displayName)
            SyncMaster.EMPTY
        }

        if (master != SyncMaster.EMPTY) {
            printInfo("Sync master info...")
            with(master) {
                printInfo("Directory: '$dir'")
                printInfo("File: '$file'")
                printInfo("Version: '$version'")
                printBlankLine()

                if (versions != VersionMasterConfig.EMPTY) {
                    printInfo("Version provider properties...")
                    with(versions) {
                        with(providers) {
                            if (isNotEmpty()) {
                                forEach { provider ->
                                    if (provider != VersionProvider.EMPTY) {
                                        with(provider) {
                                            printInfo("Sorting provider '$name' properties")
                                            if (read.isNotEmpty()) read.sortedBy { it.index }
                                            if (uris.isNotEmpty()) uris.sortedBy { it.index }
                                            if (write.isNotEmpty()) write.sortedBy { it.index }
                                        }
                                    }
                                }
                                printBlankLine()
                            }
                        }
                    }
                }
            }

            val projects = try {
                if (projectPath.isNotBlank()) {
                    getSyncProjects(getPathAsFile(context.workspaceDir.absolutePath, projectPath))
                } else {
                    getSyncProjects(context.workspaceDir)
                }
            } catch (e: Exception) {
                printException("Unable to parse project sync files", e)
                printComplete(taskName)
                emptyList()
            }

            if (projects.isNotEmpty()) {
                projects.forEach { project ->
                    val buildInfo = try {
                        getBuildInfo(project.dir)
                    } catch (e: Exception) {
                        printException("Unable to get build info", e)
                        BuildInfo.EMPTY
                    }

                    with(project) {
                        printInfo("Sync project info...")
                        printInfo("Name: '$name'")
                        printInfo("Directory: '$dir'")
                        printInfo("File: '$file'")
                        printInfo("Version: '$version'")
                        printBlankLine()

                        if (config != ConfigInfo.EMPTY) {
                            with(config) {
                                printInfo("Config info...")
                                printInfo("Enabled: '$enabled'")
                                printBlankLine()
                            }
                        }

                        if (git != GitInfo.EMPTY) {
                            with(git) {
                                printInfo("Git info...")
                                printInfo("Remote: '$remote'")
                                printInfo("Branch: '$branch'")
                                printBlankLine()
                            }
                        }

                        if (buildInfo != BuildInfo.EMPTY) {
                            with(buildInfo) {
                                printInfo("Build info...")
                                printInfo("Language: '$language'")
                                printInfo("Tool: '$tool'")
                                printInfo("Files:")
                                if (files.isNotEmpty()) {
                                    files.forEach { file ->
                                        printInfo("* $file")
                                    }
                                }
                                printBlankLine()
                            }
                        }
                    }

                    if (master.config.enabled && project.config.enabled) {
                        task.invoke(context, master, project, buildInfo)
                    } else {
                        printInfo("Project syncing disabled. Skipping...")
                        printBlankLine()
                    }

                    printSuccess("Completed sync of $displayName for project: '${project.name}'")
                    printBlankLine()
                    printDivider()
                }
            }
            printSuccess("Successfully synced $displayName")
            printComplete(displayName)
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
            Style.Header,
            true,
            "***************************************************",
            "$name task",
            "***************************************************",
        )

    protected fun printInfo(message: String) =
        print(Style.Info, false, message)

    protected fun printProgress(message: String) =
        print(Style.ProgressStatus, false, message)

    protected fun printSuccess(message: String) =
        print(Style.Success, false, message)

    protected fun printWarning(message: String) =
        print(Style.Description, false, message)

    protected fun printWarning(message: String, e: Exception) =
        print(Style.Description, false, "$message: ${e.message}")

    private fun print(style: Style, blankLine: Boolean, vararg messages: String) =
        with(out.style(style)) {
            messages.forEach { println(it) }
            if (blankLine) println()
        }
}
