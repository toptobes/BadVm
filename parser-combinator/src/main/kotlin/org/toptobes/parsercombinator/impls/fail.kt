@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> fail(error: ErrorResult) = Parser { oldState ->
    errored<R>(oldState, error)
}
