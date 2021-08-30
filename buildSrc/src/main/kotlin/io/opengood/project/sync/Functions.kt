package io.opengood.project.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.opengood.project.sync.enumeration.BuildGradleType
import io.opengood.project.sync.enumeration.CiProviderType
import io.opengood.project.sync.enumeration.LanguageSrcDirType
import io.opengood.project.sync.enumeration.LanguageType
import io.opengood.project.sync.enumeration.SettingsGradleType
import io.opengood.project.sync.enumeration.SyncFileType
import io.opengood.project.sync.model.BuildInfo
import io.opengood.project.sync.model.CiProvider
import io.opengood.project.sync.model.SyncContext
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path

internal fun buildGradleType(dir: File): BuildGradleType =
    when {
        isKotlin(dir) -> BuildGradleType.KOTLIN
        else -> BuildGradleType.GROOVY
    }

internal fun createContext(workspaceDir: String): SyncContext {
    val dir = Path.of(workspaceDir).toFile()
    (!dir.exists()) then { throw FileNotFoundException("Workspace directory cannot be found: $dir") }
    return SyncContext(workspaceDir = dir)
}

internal fun getBuildInfo(dir: File): BuildInfo =
    BuildInfo(
        language = languageType(dir),
        buildGradle = buildGradleType(dir),
        settingsGradle = settingsGradleType(dir)
    )

internal fun SyncMaster.getCiProvider(provider: CiProviderType): CiProvider =
    ci.providers.first { it.name == provider }

internal fun getSyncMaster(dir: File): SyncMaster {
    val file = Path.of("$dir/${SyncFileType.MASTER}").toFile()
    (!file.exists()) then { throw FileNotFoundException("Sync master file cannot be found: $file") }
    return getSyncObject<SyncMaster>(file).apply {
        this.dir = file.parentFile
        this.file = file
    }
}

internal inline fun <reified T : Any> getSyncObject(file: File): T {
    val objectMapper = ObjectMapper(YAMLFactory())
    objectMapper.registerModule(kotlinModule())
    return objectMapper.readValue(file, T::class.java)
}

internal fun getSyncProjects(context: SyncContext): List<SyncProject> =
    context.workspaceDir.walkTopDown()
        .filter { it.name == SyncFileType.PROJECT.toString() }
        .toList()
        .map { file ->
            getSyncObject<SyncProject>(file).apply {
                this.name = file.parentFile.name
                this.dir = file.parentFile
                this.file = file
            }
        }

internal fun isGroovy(dir: File): Boolean =
    Path.of(dir.absolutePath, LanguageSrcDirType.GROOVY.toString()).toFile().exists()

internal fun isJava(dir: File): Boolean =
    Path.of(dir.absolutePath, LanguageSrcDirType.JAVA.toString()).toFile().exists() ||
        Path.of(dir.absolutePath, BuildGradleType.GROOVY.toString()).toFile().exists()

internal fun isKotlin(dir: File): Boolean =
    Path.of(dir.absolutePath, LanguageSrcDirType.KOTLIN.toString()).toFile().exists() ||
        Path.of(dir.absolutePath, BuildGradleType.KOTLIN.toString()).toFile().exists()

internal fun languageType(dir: File): LanguageType =
    when {
        isKotlin(dir) -> LanguageType.KOTLIN
        isGroovy(dir) -> LanguageType.GROOVY
        isJava(dir) -> LanguageType.JAVA
        else -> throw IllegalStateException("Unable to detect LanguageType from project")
    }

internal fun settingsGradleType(dir: File): SettingsGradleType =
    when {
        isKotlin(dir) -> SettingsGradleType.KOTLIN
        else -> SettingsGradleType.GROOVY
    }

internal infix fun <T> Boolean.then(param: () -> T): T? =
    if (this) param() else null
