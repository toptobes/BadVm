@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> upto(
    max: Int,
    parser: Parser<R>,
    requireMatch: Boolean = false
) = Parser { oldState ->
    val results = mutableListOf<R>()
    var nextState: ParseState<*> = oldState

    for (i in 0 until max) {
        val testState = parser.parsePropagating(nextState)

        if (testState.isErrored()) {
            break
        }
        nextState = testState
        results += nextState.result
    }

    if (requireMatch && results.isEmpty()) {
        return@Parser errored(oldState, "repeatedly: Could not match any times")
    }

    return@Parser success(nextState, results)
}
