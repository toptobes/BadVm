@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser

class lazy<T, R>(val thunk: () -> Parser<T, R>) : Parser<T, R>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out R> {
        return thunk().parsePropagating(oldState)
    }
}
