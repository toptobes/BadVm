package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class str(val pattern: String) : Parser<String, String>() {
    constructor(pattern: Char) : this(pattern.toString())

    override fun parse(oldState: ParseState<String, *>): ParseState<String, out String> {
        val subtarget = oldState.target.substring(oldState.index)

        if (subtarget.isEmpty()) {
            return errored(oldState, EndOfInputError("str", oldState.index))
        }

        if (!subtarget.startsWith(pattern)) {
            return errored(oldState, MatchError("str", oldState.index, pattern))
        }

        val newIndex = oldState.index + pattern.length
        return success(oldState, pattern, newIndex)
    }
}
