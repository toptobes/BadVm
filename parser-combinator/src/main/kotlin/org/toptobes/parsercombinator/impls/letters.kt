package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

object letters : Parser<String, String>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out String> {
        val subtarget = oldState.target.substring(oldState.index)

        if (subtarget.isEmpty()) {
            return errored(oldState, EndOfInputError("letters", oldState.index))
        }

        val letters = subtarget.takeWhile(Char::isLetter)

        if (letters.isEmpty()) {
            return errored(oldState, MatchError("letters", oldState.index, "letters"))
        }

        return success(oldState, letters, oldState.index + letters.length)
    }
}
