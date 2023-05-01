@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> until(
    parser: Parser<R>,
    checkAtEndOfLoop: Boolean = false,
    condition: (ParseState<out R>) -> Boolean,
) = Parser { oldState ->
    val results = mutableListOf<R>()
    var nextState: ParseState<*> = oldState

    while (true) {
        val testState = parser.parsePropagating(nextState)

        if (!checkAtEndOfLoop && condition(testState))
            break

        if (testState.isErrored) {
            return@Parser errored(testState)
        }

        nextState = testState
        results += nextState.result!!

        if (checkAtEndOfLoop && condition(testState))
            break
    }

    return@Parser succeed(nextState, results)
}
