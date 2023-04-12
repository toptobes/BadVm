package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class fail<Target, T>(val error: ErrorResult) : Parser<Target, T>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out T> {
        return errored(oldState, error)
    }
}
