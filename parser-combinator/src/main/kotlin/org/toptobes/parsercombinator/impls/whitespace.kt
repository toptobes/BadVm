package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

val whitespace = Parser { oldState ->
    val subtarget = oldState.target.substring(oldState.index)

    if (subtarget.isEmpty()) {
        return@Parser errored(oldState,  "whitespace: EOF reading whitespace")
    }

    val whitespace = subtarget.takeWhile(Char::isWhitespace)

    if (whitespace.isEmpty()) {
        return@Parser errored(oldState, "whitespace: Could not match whitespace")
    }

    return@Parser success(oldState, whitespace, oldState.index + whitespace.length)
}

val optionalWhitespace = -whitespace
