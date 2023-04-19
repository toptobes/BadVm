@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class any<T, R>(
    vararg val parsers: Parser<T, R>
) : Parser<T, R>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out R> {
        val errors = mutableListOf<ErrorResult?>()

        for (parser in parsers) {
            val nextState = parser.parsePropagating(oldState)

            if (nextState.isOkay) {
                return nextState
            } else {
                errors += nextState.error
            }
        }

        return errored(oldState, NoNonErrorsError("any", oldState.index, errors))
    }
}
