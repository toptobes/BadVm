package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

object digits : Parser<String, String>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out String> {
        val subtarget = oldState.target.substring(oldState.index)

        if (subtarget.isEmpty()) {
            return errored(oldState, EndOfInputError("digits", oldState.index))
        }

        val digits = subtarget.takeWhile(Char::isDigit)

        if (digits.isEmpty()) {
            return errored(oldState, MatchError("digits", oldState.index, "digits"))
        }

        return success(oldState, digits, oldState.index + digits.length)
    }
}
