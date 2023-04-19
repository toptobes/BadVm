@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class sequence<T, R>(
    vararg val parsers: Parser<T, R>,
    val onError: OnError = CompletelyError
) : Parser<T, List<R>>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out List<R>> {
        val results = mutableListOf<R>()
        var nextState: ParseState<T, *> = oldState

        for ((index, parser) in parsers.withIndex()) {
            nextState = parser.parsePropagating(nextState)

            when (onError) {
                CompletelyError -> {
                    if (nextState.isErrored) {
                        return errored(oldState, SequenceError("Sequence", nextState.index, index, nextState.error!!))
                    }

                    results += nextState.result!!
                }
                IgnoreErrors -> {
                    if (nextState.isOkay) {
                        results += nextState.result!!
                    }
                }
            }
        }

        return success(nextState, results)
    }

    sealed interface OnError
    object CompletelyError : OnError
    object IgnoreErrors    : OnError
}
