package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class regex(val pattern: Regex) : Parser<String, String>() {
    constructor(pattern: String) : this(pattern.toRegex())

    override fun parse(oldState: ParseState<String, *>): ParseState<String, out String> {
        val subtarget = oldState.target.substring(oldState.index)

        if (subtarget.isEmpty()) {
            return errored(oldState, EndOfInputError("regex", oldState.index))
        }

        val match = pattern.matchAt(subtarget, 0)

        if (match?.value == null) {
            return errored(oldState, MatchError("regex", oldState.index, pattern.toString()))
        }

        return success(oldState, match.value, oldState.index + match.value.length)
    }
}
