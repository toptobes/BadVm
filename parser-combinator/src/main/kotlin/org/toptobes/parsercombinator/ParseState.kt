package org.toptobes.parsercombinator

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class ParseState<out R> {
    abstract val target: String
    abstract val index: Int
}

data class OkayParseState<out R>(
    val result: R,
    override val target: String,
    override val index: Int,
) : ParseState<R>()

data class ErroredParseState(
    val error: String,
    override val target: String,
    override val index: Int,
) : ParseState<Nothing>()

@ExperimentalContracts
fun <R> ParseState<R>.isOkay(): Boolean {
    contract {
        returns(true)  implies (this@isOkay is OkayParseState<R>)
        returns(false) implies (this@isOkay is ErroredParseState)
    }
    return this is OkayParseState
}

@ExperimentalContracts
fun <R> ParseState<R>.isErrored(): Boolean {
    contract {
        returns(true)  implies (this@isErrored is ErroredParseState)
        returns(false) implies (this@isErrored is OkayParseState<R>)
    }
    return this is ErroredParseState
}

@Suppress("FunctionName")
fun <R> UnitParseState(target: String): OkayParseState<R> {
    return OkayParseState(null as R, target, 0)
}

fun <R> errored(state: ParseState<*>, error: String): ParseState<R> {
    return ErroredParseState(error, state.target, state.index)
}

fun <R> errored(state: ErroredParseState, error: String = state.error): ParseState<R> {
    return ErroredParseState(error, state.target, state.index)
}

fun <R> success(state: ParseState<*>, result: R, index: Int = state.index): ParseState<R> {
    return OkayParseState(result, state.target, index)
}

fun <R> success(state: OkayParseState<R>): ParseState<R> {
    return state
}

fun ParseState<*>.subtarget(): String {
    return target.substring(index)
}
