@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class repeat<T, R>(val parser: Parser<T, R>, val times: Int) : Parser<T, List<R>>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out List<R>> {
        return sequence(*Array(times) { parser }).parsePropagating(oldState)
    }
}
