@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

val char = Parser { oldState ->
    val char = oldState.target.getOrNull(oldState.index)
        ?: return@Parser errored(oldState, EndOfInputError("char", oldState.index))

    return@Parser succeed(oldState, char, oldState.index + 1)
}
