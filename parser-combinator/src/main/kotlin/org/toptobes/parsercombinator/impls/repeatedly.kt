package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class repeatedly<Target, NewT>(val parser: Parser<Target, NewT>, val requireMatch: Boolean = false) : Parser<Target, List<NewT>>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out List<NewT>> {
        val results = mutableListOf<NewT>()
        var nextState: ParseState<Target, *> = oldState

        while (true) {
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
