package org.toptobes.parsercombinator

fun <R> errored(state: ParseState<*>, error: ErrorResult = state.error!!): ParseState<R> {
    return ParseState(null, state.target, state.index, error)
}

fun <R> succeed(state: ParseState<*>, result: R?, index: Int = state.index): ParseState<R> {
    return ParseState(result, state.target, index, null)
}

val ParseState<*>.isErrored
    get() = this.error != null

val ParseState<*>.isOkay
    get() = this.error == null
