@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

object nextLetter : Parser<String, String>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out String> {
        val char = oldState.target.getOrNull(oldState.index)
            ?: return errored(oldState, EndOfInputError("nextLetter", oldState.index))

        return success(oldState, char.toString(), oldState.index + 1)
    }
}
