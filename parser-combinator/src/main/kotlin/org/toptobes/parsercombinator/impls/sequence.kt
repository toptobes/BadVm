@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

operator fun <R, R2> Parser<R>.rangeTo(mapper: (R) -> R2) =
    this.map(mapper)

infix fun <R> Parser<R>.rangeTo(other: Parser<R>) =
    sequence(this, other)

infix fun <R> Parser<List<R>>.rangeTo(other: Parser<R>) =
    this.chain { list -> other.map { list + it } }

fun <R> sequence(
    vararg parsers: Parser<R>,
    onError: OnError = CompletelyError
) = Parser { oldState ->
    val results = mutableListOf<R>()
    var nextState: ParseState<*> = oldState

    for ((index, parser) in parsers.withIndex()) {
        nextState = parser.parsePropagating(nextState)

        when (onError) {
            CompletelyError -> {
                if (nextState.isErrored) {
                    return@Parser errored(
                        oldState,
                        SequenceError("Sequence", nextState.index, index, nextState.error!!)
                    )
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

    return@Parser succeed(nextState, results)
}

sealed interface OnError
object CompletelyError : OnError
object IgnoreErrors : OnError
