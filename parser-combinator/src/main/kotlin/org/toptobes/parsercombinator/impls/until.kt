@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class until<T, R>(
    val parser: Parser<T, R>,
    val checkAtEndOfLoop: Boolean = false,
    val condition: (ParseState<T, out R>) -> Boolean,
) : Parser<T, List<R>>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out List<R>> {
        val results = mutableListOf<R>()
        var nextState: ParseState<T, *> = oldState

        while (true) {
            val testState = parser.parsePropagating(nextState)

            if (!checkAtEndOfLoop && condition(testState))
                break

            if (testState.isErrored) {
                return errored(testState)
            }

            nextState = testState
            results += nextState.result!!

            if (checkAtEndOfLoop && condition(testState))
                break
        }

        return success(nextState, results)
    }
}
