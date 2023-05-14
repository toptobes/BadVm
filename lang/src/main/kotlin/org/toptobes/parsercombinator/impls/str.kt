@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun str(pattern: Char): Parser<String> {
    return str(pattern.toString())
}

fun str(pattern: String) = Parser { oldState ->
    val subtarget = oldState.target.substring(oldState.index)

    if (subtarget.isEmpty()) {
        return@Parser errored(oldState, "str: EOF reading pattern")
    }

    if (!subtarget.startsWith(pattern)) {
        return@Parser errored(oldState, "str: Could not match string $pattern")
    }

    val newIndex = oldState.index + pattern.length
    return@Parser success(oldState, pattern, index = newIndex)
}
