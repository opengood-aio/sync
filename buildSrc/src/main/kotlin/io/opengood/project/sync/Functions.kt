package io.opengood.project.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.opengood.project.sync.enumeration.BuildGradleType
import io.opengood.project.sync.enumeration.BuildToolType
import io.opengood.project.sync.enumeration.CiProviderType
import io.opengood.project.sync.enumeration.LanguageSrcDirType
import io.opengood.project.sync.enumeration.LanguageType
import io.opengood.project.sync.enumeration.MavenFileType
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
import java.util.regex.Pattern

internal fun buildGradleType(dir: File): BuildGradleType =
    when {
        isGradleGroovyDsl(dir) -> BuildGradleType.GROOVY
        isGradleKotlinDsl(dir) -> BuildGradleType.KOTLIN
        else -> BuildGradleType.NONE
    }

internal fun buildTool(dir: File): BuildToolType =
    when {
        isGradle(dir) -> BuildToolType.GRADLE
        isMaven(dir) -> BuildToolType.MAVEN
        else -> BuildToolType.UNKNOWN
    }

internal fun countSpaces(line: String): Int {
    val matcher = Pattern.compile("^\\s+").matcher(line)
    return if (matcher.find()) matcher.group(0).length else 0
}

internal fun createContext(workspaceDir: String): SyncContext {
    val dir = Path.of(workspaceDir).toFile()
    if (!dir.exists()) {
        throw FileNotFoundException("Workspace directory cannot be found: $dir")
    }
    return SyncContext(workspaceDir = dir)
}

internal fun getBuildInfo(dir: File): BuildInfo =
    BuildInfo(
        buildTool = buildTool(dir),
        language = languageType(dir),
        buildGradle = buildGradleType(dir),
        settingsGradle = settingsGradleType(dir),
        mavenFile = mavenFile(dir)
    )

internal fun SyncMaster.getCiProvider(provider: CiProviderType): CiProvider =
    ci.providers.first { it.name == provider }

internal fun getSyncMaster(dir: File): SyncMaster {
    val file = Path.of("$dir/${SyncFileType.MASTER}").toFile()
    if (!file.exists()) {
        throw FileNotFoundException("Sync master file cannot be found: $file")
    }
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

internal fun getSyncProjects(context: SyncContext, selectedProject: String): List<SyncProject> =
    context.workspaceDir.walkTopDown()
        .filter { it.name == SyncFileType.PROJECT.toString() }
        .filter {
            selectedProject.isBlank() || it.parentFile.absolutePath == Path.of(
                context.workspaceDir.absolutePath,
                selectedProject
            ).toString()
        }
        .toList()
        .map { file ->
            getSyncObject<SyncProject>(file).apply {
                this.name = file.parentFile.name
                this.dir = file.parentFile
                this.file = file
            }
        }

internal fun isGradle(dir: File): Boolean =
    Path.of(dir.absolutePath, BuildGradleType.GROOVY.toString()).toFile().exists() ||
        Path.of(dir.absolutePath, BuildGradleType.KOTLIN.toString()).toFile().exists() ||
        Path.of(dir.absolutePath, SettingsGradleType.GROOVY.toString()).toFile().exists() ||
        Path.of(dir.absolutePath, SettingsGradleType.KOTLIN.toString()).toFile().exists()

internal fun isGradleGroovyDsl(dir: File): Boolean =
    Path.of(dir.absolutePath, BuildGradleType.GROOVY.toString()).toFile().exists() ||
        Path.of(dir.absolutePath, SettingsGradleType.GROOVY.toString()).toFile().exists()

internal fun isGradleKotlinDsl(dir: File): Boolean =
    Path.of(dir.absolutePath, BuildGradleType.KOTLIN.toString()).toFile().exists() ||
        Path.of(dir.absolutePath, SettingsGradleType.KOTLIN.toString()).toFile().exists()

internal fun isGroovy(dir: File): Boolean =
    Path.of(dir.absolutePath, LanguageSrcDirType.GROOVY.toString()).toFile().exists()

internal fun isJava(dir: File): Boolean =
    Path.of(dir.absolutePath, LanguageSrcDirType.JAVA.toString()).toFile().exists()

internal fun isKotlin(dir: File): Boolean =
    Path.of(dir.absolutePath, LanguageSrcDirType.KOTLIN.toString()).toFile().exists()

internal fun isMaven(dir: File): Boolean =
    Path.of(dir.absolutePath, MavenFileType.POM.toString()).toFile().exists()

internal fun languageType(dir: File): LanguageType =
    when {
        isKotlin(dir) -> LanguageType.KOTLIN
        isGroovy(dir) -> LanguageType.GROOVY
        isJava(dir) -> LanguageType.JAVA
        else -> LanguageType.UNKNOWN
    }

internal fun mavenFile(dir: File): MavenFileType =
    when {
        isMaven(dir) -> MavenFileType.POM
        else -> MavenFileType.NONE
    }

internal fun padSpaces(line: String, spaces: Int) =
    if (spaces == 0) line else line.padStart(line.length + spaces)

internal fun settingsGradleType(dir: File): SettingsGradleType =
    when {
        isGradleGroovyDsl(dir) -> SettingsGradleType.GROOVY
        isGradleKotlinDsl(dir) -> SettingsGradleType.KOTLIN
        else -> SettingsGradleType.NONE
    }
