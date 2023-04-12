package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.success

class succeed<Target, T>(val value: T) : Parser<Target, T>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out T> {
        return success(oldState, value)
    }
}
