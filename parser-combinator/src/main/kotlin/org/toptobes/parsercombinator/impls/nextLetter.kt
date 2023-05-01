@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

val nextLetter = Parser { oldState ->
    val char = oldState.target.getOrNull(oldState.index)
        ?: return@Parser errored(oldState, EndOfInputError("nextLetter", oldState.index))

    return@Parser succeed(oldState, char.toString(), oldState.index + 1)
}
