@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class upto<T, R>(
    val max: Int,
    val parser: Parser<T, R>,
    val requireMatch: Boolean = false
) : Parser<T, List<R>>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out List<R>> {
        val results = mutableListOf<R>()
        var nextState: ParseState<T, *> = oldState

        for (i in 0 until max) {
            val testState = parser.parsePropagating(nextState)

            if (testState.isErrored) {
                break
            }
            nextState = testState
            results += nextState.result!!
        }

        if (requireMatch && results.isEmpty()) {
            return errored(oldState, NoMatchError("repeatedly", oldState.index))
        }

        return success(nextState, results)
    }
}
