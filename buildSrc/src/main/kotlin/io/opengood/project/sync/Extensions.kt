package io.opengood.project.sync

internal fun <E : Enum<E>> Enum<E>.containsAny(vararg items: Enum<E>): Boolean =
    items.any { it == this }

internal fun <E : Enum<E>> List<Enum<E>>.containsAny(vararg items: Enum<E>): Boolean =
    this.any { e -> items.any { it == e } }

internal inline fun <reified T : Any> List<T>.firstOrDefault(predicate: (T) -> Boolean, default: T): T =
    this.firstOrNull { predicate.invoke(it) } ?: default

internal inline fun <reified E : Enum<E>> List<Enum<E>>.toDelimiter(): String =
    this.joinToString(", ")
