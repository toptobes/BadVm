package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

val digits = Parser { oldState ->
    val subtarget = oldState.subtarget()

    if (subtarget.isEmpty()) {
        return@Parser errored(oldState, "digits: EOF reading digit")
    }

    val digits = subtarget.takeWhile(Char::isDigit)

    if (digits.isEmpty()) {
        return@Parser errored(oldState, "digits: Could not match a digit")
    }

    return@Parser success(oldState, digits, index = oldState.index + digits.length)
}
