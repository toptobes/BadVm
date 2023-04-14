package org.toptobes.parsercombinator

import org.toptobes.parsercombinator.impls.*

fun <Target, NewT> errored(state: ParseState<Target, *>, error: ErrorResult = state.error!!): ParseState<Target, NewT> {
    return ParseState(null, state.target, state.index, error)
}

fun <Target, NewT> success(state: ParseState<Target, *>, result: NewT?, index: Int = state.index): ParseState<Target, NewT> {
    return ParseState(result, state.target, index, null)
}

val ParseState<*, *>.isErrored
    get() = this.error != null

val ParseState<*, *>.isOkay
    get() = this.error == null

operator fun <Target, NewT, MappedT> Parser<Target, NewT>.rangeTo(mapper: (NewT) -> MappedT) =
    this.map(mapper)

infix fun <Target, NewT> Parser<Target, NewT>.then(other: Parser<Target, NewT>) =
    sequence(this, other)

infix fun <Target, NewT> sequence<Target, NewT>.then(other: Parser<Target, NewT>) =
    this.chain { list -> other.map { list + it } }

operator fun <Target, NewT> Parser<Target, NewT>.unaryPlus() =
    repeatedly(this)

operator fun <Target, NewT> Parser<Target, NewT>.not() =
    optionally(this)

operator fun <NewT> Parser<String, NewT>.unaryMinus() =
    between(!whitespace, this)

operator fun <Target,NewT> Parser<Target, NewT>.times(times: Int) =
    repeat(this, times)
