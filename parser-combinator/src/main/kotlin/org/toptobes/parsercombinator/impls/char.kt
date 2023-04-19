@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

object char : Parser<String, Char>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out Char> {
        val char = oldState.target.getOrNull(oldState.index)
            ?: return errored(oldState, EndOfInputError("char", oldState.index))

        return success(oldState, char, oldState.index + 1)
    }
}
