@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*
import java.util.*

infix fun <R> Parser<R>.or(other: Parser<R>) =
    any(this, other)

fun <R> any(vararg parsers: Parser<R>) = Parser { oldState ->
    val errors = mutableListOf<ErrorResult?>()

    for (parser in parsers) {
        val nextState = parser.parsePropagating(oldState)

        if (nextState.isOkay) {
            return@Parser nextState
        } else {
            errors += nextState.error
        }
    }

    return@Parser errored(oldState, NoNonErrorsError("any", oldState.index, errors))
}
