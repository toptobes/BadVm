@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class fail<T, R>(val error: ErrorResult) : Parser<T, R>() {
    constructor(desc: String) : this(BasicErrorResult(desc))

    override fun parse(oldState: ParseState<T, *>): ParseState<T, out R> {
        return errored(oldState, error)
    }
}
