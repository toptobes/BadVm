package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class until<Target, NewT>(val parser: Parser<Target, NewT>, val condition: (ParseState<Target, out NewT>) -> Boolean) : Parser<Target, List<NewT>>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out List<NewT>> {
        val results = mutableListOf<NewT>()
        var nextState: ParseState<Target, *> = oldState

        while (true) {
            val testState = parser.parsePropagating(nextState)

            if (condition(testState)) {
                break
            }
            if (testState.isErrored) {
                return errored(testState)
            }

            nextState = testState
            results += nextState.result!!
        }

        return success(nextState, results)
    }
}
