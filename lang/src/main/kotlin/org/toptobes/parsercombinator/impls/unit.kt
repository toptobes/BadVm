package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.ParseState
import org.toptobes.parsercombinator.Parser

@Suppress("UNCHECKED_CAST")
fun <R> unit() = Parser { oldState ->
    oldState as ParseState<R>
}
