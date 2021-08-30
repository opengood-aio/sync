import io.opengood.project.sync.task.SyncCiPipelines
import io.opengood.project.sync.task.SyncPlugins
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

    register<SyncCiPipelines>(SyncCiPipelines.TASK_NAME) {
        workspaceDir = workspace
    }

    register<SyncPlugins>(SyncPlugins.TASK_NAME) {
        workspaceDir = workspace
    }
}
