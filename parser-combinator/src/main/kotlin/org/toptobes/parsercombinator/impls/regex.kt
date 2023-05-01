@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun regex(pattern: String, matchIndex: Int = 0): Parser<String> {
    return regex(pattern.toRegex(), matchIndex)
}

fun regex(pattern: Regex, matchIndex: Int = 0) = Parser { oldState ->
    val subtarget = oldState.target.substring(oldState.index)

    if (subtarget.isEmpty()) {
        return@Parser errored(oldState, EndOfInputError("regex", oldState.index))
    }

    val match = pattern.matchAt(subtarget, 0)

    if (match?.value == null) {
        return@Parser errored(oldState, MatchError("regex", oldState.index, pattern.toString()))
    }

    return@Parser succeed(oldState, match.groupValues[matchIndex], oldState.index + match.value.length)
}
