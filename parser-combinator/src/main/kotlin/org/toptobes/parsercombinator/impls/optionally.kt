@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.isOkay
import org.toptobes.parsercombinator.success

class optionally<T, R>(
    val parser: Parser<T, R>,
    val default: R
) : Parser<T, R>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out R> {
        val newState = parser.parsePropagating(oldState)

        val result = if (newState.isOkay) newState.result else default
        val index  = if (newState.isOkay) newState.index  else oldState.index
        return success(newState, result, index)
    }
}
