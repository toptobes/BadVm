@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.success

class succeed<T, R>(val value: R) : Parser<T, R>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out R> {
        return success(oldState, value)
    }
}
