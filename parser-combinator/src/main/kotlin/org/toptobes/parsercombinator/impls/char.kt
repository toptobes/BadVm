package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

object char : Parser<String, Char>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out Char> {
        val subtarget = oldState.target.substring(oldState.index)

        if (subtarget.isEmpty()) {
            return errored(oldState, EndOfInputError("char", oldState.index))
        }

        val char = subtarget.first()
        return success(oldState, char, oldState.index + 1)
    }
}
