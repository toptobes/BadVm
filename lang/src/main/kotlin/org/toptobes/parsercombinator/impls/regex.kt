@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun regex(pattern: String, matchIndex: Int = 0): Parser<String> {
    return regex(pattern.toRegex(), matchIndex)
}

fun regex(pattern: Regex, matchIndex: Int = 0) = Parser { oldState ->
    val subtarget = oldState.subtarget()

    if (subtarget.isEmpty()) {
        return@Parser errored(oldState, "regex: EOF matching regex")
    }

    val match = pattern.matchAt(subtarget, 0)

    if (match?.value == null) {
        return@Parser errored(oldState, "regex: Couldn't match given pattern ${pattern.pattern}")
    }

    return@Parser success(oldState, match.groupValues[matchIndex], index = oldState.index + match.value.length)
}
