package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.isOkay
import org.toptobes.parsercombinator.success

class optionally<Target, NewT>(val parser: Parser<Target, NewT>, val default: NewT) : Parser<Target, NewT>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out NewT> {
        val newState = parser.parsePropagating(oldState)

        val result = if (newState.isOkay) newState.result else default
        val index  = if (newState.isOkay) newState.index  else oldState.index
        return success(newState, result, index)
    }
}
