package io.opengood.project.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.opengood.project.sync.enumeration.BuildToolType
import io.opengood.project.sync.enumeration.FileType
import io.opengood.project.sync.enumeration.FileType.BUILD_GRADLE_GROOVY
import io.opengood.project.sync.enumeration.FileType.BUILD_GRADLE_KOTLIN
import io.opengood.project.sync.enumeration.FileType.GRADLE_WRAPPER_PROPERTIES
import io.opengood.project.sync.enumeration.FileType.MAVEN_POM
import io.opengood.project.sync.enumeration.FileType.MAVEN_WRAPPER_PROPERTIES
import io.opengood.project.sync.enumeration.FileType.SETTINGS_GRADLE_GROOVY
import io.opengood.project.sync.enumeration.FileType.SETTINGS_GRADLE_KOTLIN
import io.opengood.project.sync.enumeration.FileType.VERSIONS_PROPERTIES
import io.opengood.project.sync.enumeration.LanguageType
import io.opengood.project.sync.enumeration.SrcDirType
import io.opengood.project.sync.enumeration.SyncFileType
import io.opengood.project.sync.model.BuildInfo
import io.opengood.project.sync.model.SyncContext
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncProject
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.regex.Pattern

internal fun countSpaces(line: String): Int {
    val matcher = Pattern.compile("^\\s+").matcher(line)
    return if (matcher.find()) matcher.group(0).length else 0
}

internal fun createContext(workspacePath: String, syncProjectPath: String): SyncContext {
    val workspaceDir = getPathAsFile(workspacePath)
    if (!workspaceDir.exists()) {
        throw FileNotFoundException("Workspace directory cannot be found: $workspaceDir")
    }

    val syncProjectDir = getPathAsFile(syncProjectPath)
    if (!syncProjectDir.exists()) {
        throw FileNotFoundException("Sync project directory cannot be found: $syncProjectDir")
    }

    return SyncContext(
        workspaceDir = workspaceDir,
        syncProjectDir = syncProjectDir
    )
}

internal fun getBuildFiles(dir: File): List<FileType> {
    val files = mutableListOf<FileType>()
    val buildTool = getBuildTool(dir)
    return when (buildTool) {
        BuildToolType.GRADLE -> {
            if (hasPath(dir, BUILD_GRADLE_GROOVY)) files.add(BUILD_GRADLE_GROOVY)
            if (hasPath(dir, BUILD_GRADLE_KOTLIN)) files.add(BUILD_GRADLE_KOTLIN)
            if (hasPath(dir, GRADLE_WRAPPER_PROPERTIES)) files.add(GRADLE_WRAPPER_PROPERTIES)
            if (hasPath(dir, SETTINGS_GRADLE_GROOVY)) files.add(SETTINGS_GRADLE_GROOVY)
            if (hasPath(dir, SETTINGS_GRADLE_KOTLIN)) files.add(SETTINGS_GRADLE_KOTLIN)
            if (hasPath(dir, VERSIONS_PROPERTIES)) files.add(VERSIONS_PROPERTIES)
            files
        }

        BuildToolType.MAVEN -> {
            if (hasPath(dir, MAVEN_POM)) files.add(MAVEN_POM)
            if (hasPath(dir, MAVEN_WRAPPER_PROPERTIES)) files.add(MAVEN_WRAPPER_PROPERTIES)
            files
        }

        else -> emptyList()
    }
}

internal fun getBuildInfo(dir: File): BuildInfo =
    BuildInfo(
        language = getLanguageType(dir),
        tool = getBuildTool(dir),
        files = getBuildFiles(dir)
    )

internal fun getBuildTool(dir: File): BuildToolType =
    when {
        isGradle(dir) -> BuildToolType.GRADLE
        isMaven(dir) -> BuildToolType.MAVEN
        else -> BuildToolType.UNKNOWN
    }

internal fun getFileType(file: File): FileType =
    FileType.values().first { file.name == getPathAsFile(it.toString()).name }

internal fun getGroupAsPath(group: String): String =
    group.replace(".", "/")

internal fun getLanguageType(dir: File): LanguageType =
    when {
        isKotlin(dir) -> LanguageType.KOTLIN
        isGroovy(dir) -> LanguageType.GROOVY
        isJava(dir) -> LanguageType.JAVA
        else -> LanguageType.UNKNOWN
    }

internal fun <E : Enum<E>> getPathAsFile(dir: File, file: Enum<E>): File =
    Path.of(dir.absolutePath, file.toString()).toFile()

internal fun getPathAsFile(path: String): File =
    Path.of(path).toFile()

internal fun getPathAsFile(path: String, vararg paths: String): File =
    Path.of(path, *paths).toFile()

internal fun getSyncMaster(dir: File): SyncMaster {
    val file = if (hasPath(dir, SyncFileType.MASTER_OVERRIDE)) {
        getPathAsFile(dir, SyncFileType.MASTER_OVERRIDE)
    } else {
        getPathAsFile(dir, SyncFileType.MASTER)
    }
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

internal fun getSyncProjects(dir: File): List<SyncProject> =
    dir.walkTopDown()
        .filter { it.name == SyncFileType.PROJECT.toString() }
        .toList()
        .map { file ->
            getSyncObject<SyncProject>(file).apply {
                this.name = file.parentFile.name
                this.dir = file.parentFile
                this.file = file
            }
        }

internal fun getVersionFiles(dir: File): List<File> {
    val files = mutableListOf<File>()
    if (hasPath(dir, BUILD_GRADLE_GROOVY)) files.add(getPathAsFile(dir, BUILD_GRADLE_GROOVY))
    if (hasPath(dir, BUILD_GRADLE_KOTLIN)) files.add(getPathAsFile(dir, BUILD_GRADLE_KOTLIN))
    if (hasPath(dir, GRADLE_WRAPPER_PROPERTIES)) files.add(getPathAsFile(dir, GRADLE_WRAPPER_PROPERTIES))
    if (hasPath(dir, MAVEN_POM)) files.add(getPathAsFile(dir, MAVEN_POM))
    if (hasPath(dir, MAVEN_WRAPPER_PROPERTIES)) files.add(getPathAsFile(dir, MAVEN_WRAPPER_PROPERTIES))
    if (hasPath(dir, SETTINGS_GRADLE_GROOVY)) files.add(getPathAsFile(dir, SETTINGS_GRADLE_GROOVY))
    if (hasPath(dir, SETTINGS_GRADLE_KOTLIN)) files.add(getPathAsFile(dir, SETTINGS_GRADLE_KOTLIN))
    if (hasPath(dir, VERSIONS_PROPERTIES)) files.add(getPathAsFile(dir, VERSIONS_PROPERTIES))
    return files
}

internal fun <E : Enum<E>> hasPath(dir: File, file: Enum<E>): Boolean =
    getPathAsFile(dir.absolutePath, file.toString()).exists()

internal fun hasPath(dir: File, files: List<String>): Boolean =
    files.any { getPathAsFile(dir.absolutePath, it).exists() }

internal fun isGradle(dir: File): Boolean =
    hasPath(dir, BUILD_GRADLE_GROOVY) ||
        hasPath(dir, BUILD_GRADLE_KOTLIN) ||
        hasPath(dir, SETTINGS_GRADLE_GROOVY) ||
        hasPath(dir, SETTINGS_GRADLE_KOTLIN)

internal fun isGradleGroovyDsl(dir: File): Boolean =
    hasPath(dir, BUILD_GRADLE_GROOVY) ||
        hasPath(dir, SETTINGS_GRADLE_GROOVY)

internal fun isGradleKotlinDsl(dir: File): Boolean =
    hasPath(dir, BUILD_GRADLE_KOTLIN) ||
        hasPath(dir, SETTINGS_GRADLE_KOTLIN)

internal fun isGroovy(dir: File): Boolean =
    hasPath(dir, SrcDirType.GROOVY.values)

internal fun isJava(dir: File): Boolean =
    hasPath(dir, SrcDirType.JAVA.values)

internal fun isKotlin(dir: File): Boolean =
    hasPath(dir, SrcDirType.KOTLIN.values)

internal fun isMaven(dir: File): Boolean =
    hasPath(dir, MAVEN_POM)

internal fun padSpaces(line: String, spaces: Int): String =
    if (spaces == 0) line else line.padStart(line.length + spaces)
