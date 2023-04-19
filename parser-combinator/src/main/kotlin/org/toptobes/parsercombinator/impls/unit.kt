@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser

class unit<T, R> : Parser<T, R>() {
    @Suppress("UNCHECKED_CAST")
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out R> {
        return oldState as ParseState<T, out R>
    }
}
