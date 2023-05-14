package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> fail(error: String) = Parser { oldState ->
    errored<R>(oldState, error)
}
