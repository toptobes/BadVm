@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.isOkay
import org.toptobes.parsercombinator.success

infix fun <R> Parser<R>.withDefault(default: R) = Parser { oldState ->
    val newState = this.parsePropagating(oldState)

    if (newState.isOkay()) {
        success(newState)
    } else {
        success(oldState, default)
    }
}
