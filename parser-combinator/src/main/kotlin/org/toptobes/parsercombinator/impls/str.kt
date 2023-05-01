@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun str(pattern: Char): Parser<String> {
    return str(pattern.toString())
}

fun str(pattern: String) = Parser { oldState ->
    val subtarget = oldState.target.substring(oldState.index)

    if (subtarget.isEmpty()) {
        return@Parser errored(oldState, EndOfInputError("str", oldState.index))
    }

    if (!subtarget.startsWith(pattern)) {
        return@Parser errored(oldState, MatchError("str", oldState.index, pattern))
    }

    val newIndex = oldState.index + pattern.length
    return@Parser succeed(oldState, pattern, newIndex)
}
