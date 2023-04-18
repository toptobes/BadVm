package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class any<Target, NewT>(vararg val parsers: Parser<Target, NewT>) : Parser<Target, NewT>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out NewT> {
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
