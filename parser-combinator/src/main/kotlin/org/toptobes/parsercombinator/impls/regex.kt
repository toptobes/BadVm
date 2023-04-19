@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class regex(
    val pattern: Regex,
    val matchIndex: Int = 0
) : Parser<String, String>() {
    constructor(pattern: String, matchIndex: Int = 0) : this(pattern.toRegex(), matchIndex)

    override fun parse(oldState: ParseState<String, *>): ParseState<String, out String> {
        val subtarget = oldState.target.substring(oldState.index)

        if (subtarget.isEmpty()) {
            return errored(oldState, EndOfInputError("regex", oldState.index))
        }

        val match = pattern.matchAt(subtarget, 0)

        if (match?.value == null) {
            return errored(oldState, MatchError("regex", oldState.index, pattern.toString()))
        }

        return success(oldState, match.groupValues[matchIndex], oldState.index + match.value.length)
    }
}
