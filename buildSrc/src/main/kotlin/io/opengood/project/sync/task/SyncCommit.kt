package io.opengood.project.sync.task

import com.lordcodes.turtle.shellRun
import io.opengood.project.sync.model.SyncProject
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class SyncCommit : BaseTask() {

    @Input
    lateinit var workspaceDir: String

    init {
        group = "sync"
        description = "Commits and pushes sync changes for each project"
    }

    @TaskAction
    fun run() {
        execute(
            name = TASK_NAME,
            displayName = TASK_DISPLAY_NAME,
            workspaceDir = workspaceDir,
            projectDir = project.projectDir.absolutePath
        ) { _, _, project: SyncProject, _ ->
            shellRun(project.dir) {
                if (git.status().isNotBlank()) {
                    git.commitAllChanges("Make project sync changes")
                    git.pull()
                    git.pushToOrigin()
                } else {
                    git.currentBranch()
                }
            }
        }
    }

    companion object {
        const val TASK_NAME = "syncCommit"
        const val TASK_DISPLAY_NAME = "Commit"
    }
}
