@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.isOkay
import org.toptobes.parsercombinator.succeed

operator fun Parser<String>.not() =
    this withDefault ""

infix fun <R> Parser<R>.withDefault(default: R) = Parser { oldState ->
    val newState = this.parsePropagating(oldState)

    val result = if (newState.isOkay) newState.result else default
    val index  = if (newState.isOkay) newState.index else oldState.index
    succeed(newState, result, index)
}
