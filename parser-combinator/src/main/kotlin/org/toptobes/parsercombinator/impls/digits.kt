@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

val digits = Parser { oldState ->
    val subtarget = oldState.target.substring(oldState.index)

    if (subtarget.isEmpty()) {
        return@Parser errored(oldState, EndOfInputError("digits", oldState.index))
    }

    val digits = subtarget.takeWhile(Char::isDigit)

    if (digits.isEmpty()) {
        return@Parser errored(oldState, MatchError("digits", oldState.index, "digits"))
    }

    return@Parser succeed(oldState, digits, oldState.index + digits.length)
}

