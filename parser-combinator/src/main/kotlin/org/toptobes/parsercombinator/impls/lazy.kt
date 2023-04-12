package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser

class lazy<Target, NewT>(val thunk: () -> Parser<Target, NewT>) : Parser<Target, NewT>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out NewT> {
        return thunk().parsePropagating(oldState)
    }
}
