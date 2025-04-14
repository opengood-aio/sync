package io.opengood.project.sync.task

import org.gradle.api.tasks.TaskAction

open class Sync : BaseTask() {
    init {
        group = "sync"
        description = "Syncs all artifacts for each project"
    }

    @TaskAction
    fun run() {
    }

    companion object {
        const val TASK_NAME = "sync"
    }
}
