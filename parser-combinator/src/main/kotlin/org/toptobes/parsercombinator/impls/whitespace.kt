@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

operator fun <R> Parser<R>.unaryMinus() =
    between(whitespace withDefault "", this)

val whitespace = Parser { oldState ->
    val subtarget = oldState.target.substring(oldState.index)

    if (subtarget.isEmpty()) {
        return@Parser errored(oldState, EndOfInputError("whitespace", oldState.index))
    }

    val whitespace = subtarget.takeWhile(Char::isWhitespace)

    if (whitespace.isEmpty()) {
        return@Parser errored(oldState, MatchError("whitespace", oldState.index, "whitespace"))
    }

    return@Parser succeed(oldState, whitespace, oldState.index + whitespace.length)
}

val optionalWhitespace = -whitespace
