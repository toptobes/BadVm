@file:Suppress("ClassName", "ControlFlowWithEmptyBody")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*
import java.util.*

fun <R> pool(vararg parsers: Parser<R>) = Parser { oldState ->
    val results = mutableListOf<R>()
    var nextState: ParseState<*> = oldState

    val parsersList = parsers.toMutableList()

    while (parsersList.removeIf {
            val testState = it.parsePropagating(nextState)

            if (testState.isOkay) {
                results += testState.result!!
                nextState = testState
            }

            testState.isOkay
        });

    succeed(nextState, results)
}
