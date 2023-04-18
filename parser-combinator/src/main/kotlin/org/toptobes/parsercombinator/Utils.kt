package org.toptobes.parsercombinator

import org.toptobes.parsercombinator.impls.*

fun <T, R> errored(state: ParseState<T, *>, error: ErrorResult = state.error!!): ParseState<T, R> {
    return ParseState(null, state.target, state.index, error)
}

fun <T, R> success(state: ParseState<T, *>, result: R?, index: Int = state.index): ParseState<T, R> {
    return ParseState(result, state.target, index, null)
}

val ParseState<*, *>.isErrored
    get() = this.error != null

val ParseState<*, *>.isOkay
    get() = this.error == null

operator fun <T, R, R2> Parser<T, R>.rangeTo(mapper: (R) -> R2) =
    this.map(mapper)

infix fun <T, R> Parser<T, R>.then(other: Parser<T, R>) =
    sequence(this, other)

infix fun <T, R> sequence<T, R>.then(other: Parser<T, R>) =
    this.chain { list -> other.map { list + it } }

infix fun <T, R> Parser<T, R>.withDefault(default: R) =
    optionally(this, default)

operator fun <T, R> Parser<T, R>.unaryPlus() =
    repeatedly(this)

operator fun <R> Parser<String, R>.unaryMinus() =
    between(optionally(whitespace, ""), this)

operator fun Parser<String, String>.not() =
    optionally(this, "")

operator fun <T,R> Parser<T, R>.times(times: Int) =
    repeat(this, times)

val optionalWhitespace = optionally(whitespace, "")

infix fun <T, R> Parser<T, R>.or(other: Parser<T, R>) =
    any(this, other)
