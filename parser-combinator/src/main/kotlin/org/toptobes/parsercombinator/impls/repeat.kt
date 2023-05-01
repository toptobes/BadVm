@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

operator fun <R> Parser<R>.times(times: Int) =
    repeat(this, times)

fun <R> repeat(parser: Parser<R>, times: Int) = Parser { oldState ->
    sequence(*Array(times) { parser }).parsePropagating(oldState)
}
