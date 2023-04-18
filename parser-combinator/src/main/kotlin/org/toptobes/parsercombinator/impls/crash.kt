package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*
import java.lang.IllegalStateException

class crash<Target, T>(val msg: String) : Parser<Target, T>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out T> {
        throw IllegalStateException(msg)
    }
}
