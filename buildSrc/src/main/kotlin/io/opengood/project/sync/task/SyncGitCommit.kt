package io.opengood.project.sync.task

import com.lordcodes.turtle.shellRun
import io.opengood.project.sync.model.GitInfo
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import org.apache.commons.lang3.StringUtils
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class SyncGitCommit : BaseTask() {

    @Input
    lateinit var workspacePath: String

    @Input
    lateinit var projectPath: String

    init {
        group = "sync"
        description = "Performs Git commit and push containing sync changes for each project"
    }

    @TaskAction
    fun run() {
        execute(
            taskName = TASK_NAME,
            displayName = TASK_DISPLAY_NAME,
            workspacePath = workspacePath,
            projectPath = projectPath,
        ) { _, master: SyncMaster, project: SyncProject, _ ->
            val commitMessage = if (project.git != GitInfo.EMPTY && project.git.commitMessage.isNotBlank()) {
                project.git.commitMessage
            } else {
                master.git.commitMessage.ifBlank {
                    GitInfo.DEFAULT_COMMIT_MESSAGE
                }
            }

            with(project) {
                shellRun(dir) {
                    val branch = project.git.branch.ifBlank {
                        git.currentBranch()
                    }

                    with(project.git) {
                        printInfo("Determining project changes for '$name' in local Git repo '$dir'...")
                        printBlankLine()
                        val status = git.status()

                        if (status.isNotBlank()) {
                            printInfo("Determining current Git status:")
                            printInfo(status)
                            printDone()

                            printProgress("Checking out '$branch' branch on local Git repo...")
                            git.checkout(branch)
                            printDone()

                            printProgress("Committing all changes to '$branch' branch in local Git repo...")
                            git.commitAllChanges(commitMessage)
                            printDone()

                            printProgress("Pulling potential changes from remote '$remote' in branch '$branch' Git repo...")
                            try {
                                git.pull(remote, branch)
                                printDone()
                            } catch (e: Exception) {
                                printWarning("Unable to pull changes from remote '$remote' in branch '$branch' Git repo", e)
                                printBlankLine()
                            }

                            printProgress("Pushing changes to remote '$remote' in branch '$branch' Git repo...")
                            git.push(remote, branch)
                            printDone()
                        } else {
                            printInfo("No project changes found in local Git repo. Skipping.")
                            printBlankLine()
                        }
                    }
                    StringUtils.EMPTY
                }
            }
        }
    }

    companion object {
        const val TASK_NAME = "syncGitCommit"
        const val TASK_DISPLAY_NAME = "Sync Git Commit"
    }
}
