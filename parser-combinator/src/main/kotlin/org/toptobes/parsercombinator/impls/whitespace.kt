package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

object whitespace : Parser<String, String>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out String> {
        val subtarget = oldState.target.substring(oldState.index)

        if (subtarget.isEmpty()) {
            return errored(oldState, EndOfInputError("whitespace", oldState.index))
        }

        val whitespace = subtarget.takeWhile(Char::isWhitespace)

        if (whitespace.isEmpty()) {
            return errored(oldState, MatchError("whitespace", oldState.index, "whitespace"))
        }

        return success(oldState, whitespace, oldState.index + whitespace.length)
    }
}
