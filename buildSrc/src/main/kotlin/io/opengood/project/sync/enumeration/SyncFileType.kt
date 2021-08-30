package io.opengood.project.sync.enumeration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape
import com.fasterxml.jackson.annotation.JsonValue

@JsonFormat(shape = Shape.OBJECT)
enum class SyncFileType(@JsonValue private val value: String) {
    MASTER("sync-master.yml"),
    PROJECT("sync.yml"),
    ;

    override fun toString(): String = value
}
