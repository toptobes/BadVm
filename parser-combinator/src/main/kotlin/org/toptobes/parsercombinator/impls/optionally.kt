@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.isOkay
import org.toptobes.parsercombinator.success

infix fun <R> Parser<R>.withDefault(default: R) = Parser { oldState ->
    val newState = this.parsePropagating(oldState)

    val result = if (newState.isOkay()) newState.result else default
    val index  = if (newState.isOkay()) newState.index  else oldState.index
    success(newState, result, oldState.types, oldState.vars, index)
}
