@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> sequence(
    vararg parsers: Parser<R>,
    onError: OnError = CompletelyError
) = Parser { oldState ->
    val results = mutableListOf<R>()
    var nextState = oldState

    for ((index, parser) in parsers.withIndex()) {
        val testState = parser.parsePropagating(nextState)

        when (onError) {
            CompletelyError -> {
                if (testState.isErrored()) {
                    return@Parser errored(oldState, "sequence: Errored state for parser #$index (${testState.error})")
                }
                nextState = testState
                results += nextState.result
            }
            IgnoreErrors -> {
                if (testState.isOkay()) {
                    nextState = testState
                    results += nextState.result
                }
            }
        }
    }

    return@Parser success(nextState, results)
}

sealed interface OnError
object CompletelyError : OnError
object IgnoreErrors : OnError
