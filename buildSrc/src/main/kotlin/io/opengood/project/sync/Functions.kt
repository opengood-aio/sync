package io.opengood.project.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.opengood.project.sync.constant.Files
import io.opengood.project.sync.enumeration.CiProviderType
import io.opengood.project.sync.model.CiProvider
import io.opengood.project.sync.model.SyncContext
import io.opengood.project.sync.model.SyncMaster
import io.opengood.project.sync.model.SyncMetadata
import io.opengood.project.sync.model.SyncProject
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path

internal fun createContext(workspaceDir: String): SyncContext {
    val dir = Path.of(workspaceDir).toFile()
    (!dir.exists()) then { throw FileNotFoundException("Workspace directory cannot be found: $dir") }
    return SyncContext(workspaceDir = dir)
}

internal fun SyncMaster.getCiProvider(providerType: CiProviderType): CiProvider =
    ci.providers.first { it.name == providerType }

internal fun getSyncMaster(dir: File): SyncMaster {
    val file = Path.of("$dir/${Files.SYNC_MASTER}").toFile()
    (!file.exists()) then { throw FileNotFoundException("Sync master file cannot be found: $file") }
    return getSyncObject<SyncMaster>(file).apply {
        metadata = SyncMetadata(
            dir = file.parentFile,
            syncFile = file
        )
    }
}

internal inline fun <reified T : Any> getSyncObject(file: File): T {
    val objectMapper = ObjectMapper(YAMLFactory())
    objectMapper.registerModule(kotlinModule())
    return objectMapper.readValue(file, T::class.java)
}

internal fun getSyncProjects(context: SyncContext): List<SyncProject> =
    context.workspaceDir.walkTopDown()
        .filter { it.name == Files.SYNC }
        .toList()
        .map { file ->
            val project = getSyncObject<SyncProject>(file)
            project.apply {
                name = file.parentFile.name
                metadata = SyncMetadata(
                    dir = file.parentFile,
                    syncFile = file
                )
            }
        }

internal infix fun <T> Boolean.then(param: () -> T): T? =
    if (this) param() else null
