package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class upto<Target, NewT>(val max: Int, val parser: Parser<Target, NewT>, val requireMatch: Boolean = false) : Parser<Target, List<NewT>>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out List<NewT>> {
        val results = mutableListOf<NewT>()
        var nextState: ParseState<Target, *> = oldState

        for (i in 0 until max) {
            val testState = parser.parsePropagating(nextState)

            if (testState.isErrored) {
                break
            }
            nextState = testState
            results += nextState.result!!
        }

        if (requireMatch && results.isEmpty()) {
            return errored(oldState, NoMatchError("repeatedly", oldState.index))
        }

        return success(nextState, results)
    }
}
