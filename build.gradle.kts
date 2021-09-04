import io.opengood.project.sync.task.SyncAll
import io.opengood.project.sync.task.SyncCiPipelines
import io.opengood.project.sync.task.SyncCommit
import io.opengood.project.sync.task.SyncVersions
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "io.opengood.project"

val kotlinVersion = getKotlinPluginVersion()
val javaVersion = JavaVersion.VERSION_11
val jvmTargetVersion = "11"

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

    register<SyncAll>(SyncAll.TASK_NAME) {
        dependsOn(
            SyncCiPipelines.TASK_NAME,
            SyncVersions.TASK_NAME,
            SyncCommit.TASK_NAME,
        )
    }

    register<SyncCiPipelines>(SyncCiPipelines.TASK_NAME) {
        workspaceDir = workspace
    }

    register<SyncCommit>(SyncCommit.TASK_NAME) {
        workspaceDir = workspace
    }

    register<SyncVersions>(SyncVersions.TASK_NAME) {
        workspaceDir = workspace
    }
}
