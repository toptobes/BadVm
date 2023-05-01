@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

operator fun <R> Parser<R>.unaryPlus() =
    repeatedly(this)

fun <R> repeatedly(parser: Parser<R>, requireMatch: Boolean = false) = Parser { oldState ->
    val results = mutableListOf<R>()
    var nextState: ParseState<*> = oldState

    while (true) {
        val testState = parser.parsePropagating(nextState)

        if (testState.isErrored) {
            break
        }

        nextState = testState
        results += nextState.result!!
    }

    if (requireMatch && results.isEmpty()) {
        return@Parser errored(oldState, NoMatchError("repeatedly", oldState.index))
    }

    return@Parser succeed(nextState, results)
}
