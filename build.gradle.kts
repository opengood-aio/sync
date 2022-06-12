import io.opengood.project.sync.task.Sync
import io.opengood.project.sync.task.SyncCiPipelines
import io.opengood.project.sync.task.SyncCommit
import io.opengood.project.sync.task.SyncVersions
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "io.opengood.project"
description = "Sync Tool"

val kotlinVersion = getKotlinPluginVersion()
val javaVersion = JavaVersion.VERSION_17
val jvmTargetVersion = "17"

java.apply {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(kotlinVersion)
            because("Incompatibilities with older Kotlin versions")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

fun getProperty(name: String) =
    if (project.hasProperty(name)) {
        project.property(name).toString()
    } else {
        ""
    }

with(tasks) {
    withType<Wrapper> {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = jvmTargetVersion
            }
        }
    }

    val workspace = project.projectDir.parentFile.absolutePath
    val selectedProj = getProperty("selectedProject")
    val commitMsg = project.property("commitMessage").toString()

    register<Sync>(Sync.TASK_NAME) {
        dependsOn(
            SyncCiPipelines.TASK_NAME,
            SyncVersions.TASK_NAME,
            SyncCommit.TASK_NAME
        )
    }

    register<SyncCiPipelines>(SyncCiPipelines.TASK_NAME) {
        workspaceDir = workspace
        selectedProject = selectedProj
    }

    register<SyncVersions>(SyncVersions.TASK_NAME) {
        workspaceDir = workspace
        selectedProject = selectedProj
    }

    register<SyncCommit>(SyncCommit.TASK_NAME) {
        workspaceDir = workspace
        selectedProject = selectedProj
        commitMessage = commitMsg
        mustRunAfter(SyncVersions.TASK_NAME)
    }
}
