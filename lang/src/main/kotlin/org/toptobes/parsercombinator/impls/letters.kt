package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

val letters = Parser { oldState ->
    val subtarget = oldState.subtarget()

    if (subtarget.isEmpty()) {
        return@Parser errored(oldState, "letters: EOF reading letter")
    }

    val letters = subtarget.takeWhile(Char::isLetter)

    if (letters.isEmpty()) {
        return@Parser errored(oldState, "letters: Could not match a letter")
    }

    return@Parser success(oldState, letters, index = oldState.index + letters.length)
}
