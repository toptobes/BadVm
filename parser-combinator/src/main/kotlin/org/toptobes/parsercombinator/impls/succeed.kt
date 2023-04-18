package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.success

class succeed<Target, R>(val value: R) : Parser<Target, R>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out R> {
        return success(oldState, value)
    }
}
