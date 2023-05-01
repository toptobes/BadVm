package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> until(
    parser: Parser<R>,
    checkAtEndOfLoop: Boolean = false,
    condition: (ParseState<R>) -> Boolean,
) = Parser { oldState ->
    val results = mutableListOf<R>()
    var nextState = oldState

    while (true) {
        val testState = parser.parsePropagating(nextState)

        if (!checkAtEndOfLoop && condition(testState))
            break

        if (testState.isErrored()) {
            return@Parser errored(testState)
        }

        nextState = testState
        results += nextState.result

        if (checkAtEndOfLoop && condition(testState))
            break
    }

    return@Parser success(nextState, results)
}
