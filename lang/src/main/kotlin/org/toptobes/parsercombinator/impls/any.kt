package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> any(parsers: List<Parser<R>>): Parser<R> {
    return any(*parsers.toTypedArray())
}

fun <R> any(vararg parsers: Parser<R>) = Parser { oldState ->
    val errors = parsers.fold(emptyList<String>()) { acc, parser ->
        val nextState = parser.parsePropagating(oldState)

        if (nextState.isOkay()) {
            return@Parser success(nextState)
        } else {
            acc + nextState.error
        }
    }

    return@Parser errored(oldState, "any: No parser matches; errs = $errors")
}
