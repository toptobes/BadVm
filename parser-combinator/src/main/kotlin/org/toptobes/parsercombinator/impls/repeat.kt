package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class repeat<Target, NewT>(val parser: Parser<Target, NewT>, val times: Int) : Parser<Target, List<NewT>>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out List<NewT>> {
        return sequence(*Array(times) { parser }).parsePropagating(oldState)
    }
}
