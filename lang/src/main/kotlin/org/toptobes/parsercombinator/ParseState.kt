package org.toptobes.parsercombinator

import org.toptobes.lang.ast.Interpretation
import org.toptobes.lang.ast.Symbol
import org.toptobes.lang.ast.TypeInterpretation
import org.toptobes.lang.utils.Word
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class ParseState<out R> {
    abstract val target: String
    abstract val index: Int
}

typealias TypeMap = Map<String, TypeInterpretation>
typealias Assumptions = Map<String, Interpretation>

data class VarMap(val vars: Map<String, Symbol>, val nextAddress: Word) : Map<String, Symbol> by vars

// TODO: Make a file for pretty printing everything so it doesn't get in the way of actual logic
data class OkayParseState<out R>(
    val result: R,
    val types: TypeMap,
    val vars: VarMap,
    val assumptions: Assumptions,
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

inline fun <R, R2> ParseState<R>.ifOkay(block: OkayParseState<R>.() -> R2): R2? {
    return if (this.isOkay()) block(this) else null
}

@Suppress("FunctionName", "UNCHECKED_CAST")
fun <R> UnitParseState(target: String, vars: VarMap): OkayParseState<R> {
    return OkayParseState(null as R, emptyMap(), vars, emptyMap(), target, 0)
}

fun <R> errored(state: ParseState<*>, error: String): ParseState<R> {
    return ErroredParseState(error, state.target, state.index)
}

fun <R> errored(state: ErroredParseState, error: String = state.error): ParseState<R> {
    return ErroredParseState(error, state.target, state.index)
}

fun <R> success(
    state: ParseState<*>,
    result: R,
    types: TypeMap,
    vars: VarMap,
    assumptions: Assumptions,
    index: Int = state.index
): ParseState<R> {
    return OkayParseState(result, types, vars, assumptions, state.target, index)
}

fun <R> success(
    state: OkayParseState<*>,
    result: R,
    types: TypeMap = state.types,
    vars: VarMap = state.vars,
    assumptions: Assumptions = state.assumptions,
    index: Int = state.index
): ParseState<R> {
    return OkayParseState(result, types, vars, assumptions, state.target, index)
}

fun <R> success(state: OkayParseState<R>): ParseState<R> {
    return state
}

fun ParseState<*>.subtarget(): String {
    return target.substring(index)
}
