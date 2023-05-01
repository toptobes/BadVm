@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> repeatedly(parser: Parser<R>, requireMatch: Boolean = false) = Parser { oldState ->
    val results = mutableListOf<R>()
    var nextState = oldState

    while (true) {
        val testState = parser.parsePropagating(nextState)

        if (testState.isErrored()) {
            break
        }

        nextState = testState
        results += nextState.result
    }

    if (requireMatch && results.isEmpty()) {
        return@Parser errored(oldState, "repeatedly: Parser didn't match anything")
    }

    return@Parser success(nextState, results)
}
