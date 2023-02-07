package io.opengood.project.sync

internal fun <E : Enum<E>> Enum<E>.containsAny(vararg items: Enum<E>): Boolean =
    items.any { it == this }

internal fun <E : Enum<E>> List<Enum<E>>.containsAny(vararg items: Enum<E>): Boolean =
    this.any { e -> items.any { it == e } }

internal inline fun <reified E : Enum<E>> List<Enum<E>>.toDelimiter(): String =
    this.joinToString(", ")

internal inline fun <reified E : Enum<E>> String.toEnum(): Enum<E>? =
    enumValues<E>().firstOrNull { it.toString().equals(this, ignoreCase = true) }
