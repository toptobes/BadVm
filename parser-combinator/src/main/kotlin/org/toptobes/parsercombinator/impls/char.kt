package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

val char = Parser { oldState ->
    val char = oldState.target.getOrNull(oldState.index)
        ?: return@Parser errored(oldState, "char: EOF reading char")

    return@Parser success(oldState, char, oldState.index + 1)
}
