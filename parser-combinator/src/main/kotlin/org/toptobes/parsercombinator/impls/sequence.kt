package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class sequence<Target, NewT>(vararg val parsers: Parser<Target, NewT>, val onError: OnError = CompletelyError) : Parser<Target, List<NewT>>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out List<NewT>> {
        val results = mutableListOf<NewT>()
        var nextState: ParseState<Target, *> = oldState

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
