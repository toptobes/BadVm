package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

object nextLetter : Parser<String, String>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out String> {
        val subtarget = oldState.target.substring(oldState.index)

        if (subtarget.isEmpty()) {
            return errored(oldState, EndOfInputError("char", oldState.index))
        }

        val char = subtarget.first().toString()
        return success(oldState, char, oldState.index + 1)
    }
}
