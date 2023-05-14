package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> repeatTimes(parser: Parser<R>, times: Int) = Parser { oldState ->
    sequence(*Array(times) { parser }).parsePropagating(oldState)
}
