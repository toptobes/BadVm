package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

val nextLetter = Parser { oldState ->
    val char = oldState.target.getOrNull(oldState.index)
        ?: return@Parser errored(oldState, "nextLetter: EOF reading next letter")

    return@Parser success(oldState, char.toString(), index = oldState.index + 1)
}
