import io.opengood.project.sync.task.Sync
import io.opengood.project.sync.task.SyncCiTemplates
import io.opengood.project.sync.task.SyncGitCommit
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

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:_")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")
    implementation("com.github.kittinunf.fuel:fuel:_")
    implementation("com.jayway.jsonpath:json-path:_")
    implementation("com.lordcodes.turtle:turtle:_")
    implementation("org.apache.commons:commons-lang3:_")
    implementation("org.dom4j:dom4j:_")
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
    val proj = getProperty("project")
    val commitMsg = getProperty("commitMessage")

    register<Sync>(Sync.TASK_NAME) {
        dependsOn(
            SyncCiTemplates.TASK_NAME,
            SyncVersions.TASK_NAME,
            SyncGitCommit.TASK_NAME
        )
    }

    register<SyncCiTemplates>(SyncCiTemplates.TASK_NAME) {
        workspacePath = workspace
        projectPath = proj
    }

    register<SyncVersions>(SyncVersions.TASK_NAME) {
        workspacePath = workspace
        projectPath = proj
    }

    register<SyncGitCommit>(SyncGitCommit.TASK_NAME) {
        workspacePath = workspace
        projectPath = proj
        commitMessage = commitMsg
        mustRunAfter(SyncVersions.TASK_NAME)
    }
}
