@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.Parser

fun <R> lazy(thunk: () -> Parser<R>) = Parser { oldState ->
    thunk().parsePropagating(oldState)
}
