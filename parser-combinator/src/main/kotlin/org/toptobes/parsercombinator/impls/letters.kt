@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

val letters = Parser { oldState ->
    val subtarget = oldState.target.substring(oldState.index)

    if (subtarget.isEmpty()) {
        return@Parser errored(oldState, EndOfInputError("letters", oldState.index))
    }

    val letters = subtarget.takeWhile(Char::isLetter)

    if (letters.isEmpty()) {
        return@Parser errored(oldState, MatchError("letters", oldState.index, "letters"))
    }

    return@Parser succeed(oldState, letters, oldState.index + letters.length)
}
