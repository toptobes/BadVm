@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.lang.utils.StatelessParsingException
import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser

class crash<T>(val msg: String) : Parser<T, Nothing>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out Nothing> {
        throw StatelessParsingException(msg)
    }
}
