@file:Suppress("ClassName", "ControlFlowWithEmptyBody")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*
import java.util.*

class pool<T, R>(
    vararg val parsers: Parser<T, R>
) : Parser<T, List<R>>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out List<R>> {
        val results = mutableListOf<R>()
        var nextState: ParseState<T, *> = oldState

        val parsers = this.parsers.toMutableList()

        while (parsers.removeIf {
            val testState = it.parsePropagating(nextState)

            if (testState.isOkay) {
                results += testState.result!!
                nextState = testState
            }

            testState.isOkay
        });

        return success(nextState, results)
    }
}
