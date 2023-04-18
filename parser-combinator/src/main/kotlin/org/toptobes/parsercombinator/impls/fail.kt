package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class fail<Target, T>(val error: ErrorResult? = null) : Parser<Target, T>() {
    constructor(desc: String) : this(BasicErrorResult(desc))

    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out T> {
        return errored(oldState, error ?: DEFAULT_FAIL_RESULT)
    }

    companion object {
        val DEFAULT_FAIL_RESULT = object : ErrorResult {
            override fun toString() = "DEFAULT_FAIL_RESULT"
        }
    }
}
