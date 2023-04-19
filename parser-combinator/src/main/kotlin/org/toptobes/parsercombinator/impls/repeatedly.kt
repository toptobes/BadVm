@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class repeatedly<T, R>(
    val parser: Parser<T, R>,
    val requireMatch: Boolean = false
) : Parser<T, List<R>>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out List<R>> {
        val results = mutableListOf<R>()
        var nextState: ParseState<T, *> = oldState

        while (true) {
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
