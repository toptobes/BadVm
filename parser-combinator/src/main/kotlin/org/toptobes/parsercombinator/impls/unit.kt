package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser

class unit<Target, NewR> : Parser<Target, NewR>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out NewR> {
        return oldState as ParseState<Target, out NewR>
    }
}
