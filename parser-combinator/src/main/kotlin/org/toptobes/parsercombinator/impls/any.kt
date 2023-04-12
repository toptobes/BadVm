package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class any<Target, NewT>(vararg val parsers: Parser<Target, NewT>) : Parser<Target, NewT>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out NewT> {
        for (parser in parsers) {
            val nextState = parser.parsePropagating(oldState)

            if (nextState.isOkay) {
                return nextState
            }
        }

        return errored(oldState, NoMatchError("Any", oldState.index))
    }
}
