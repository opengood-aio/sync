package io.opengood.project.sync.task

import org.gradle.api.tasks.TaskAction

open class SyncAll : BaseTask() {

    init {
        group = "sync"
        description = "Syncs all items for each project"
    }

    @TaskAction
    fun run() {
    }

    companion object {
        const val TASK_NAME = "syncAll"
    }
}
