package org.toptobes.parsercombinator

import org.toptobes.lang.ast.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class ParseState<out R> {
    abstract val target: String
    abstract val index: Int
}

typealias Types = Map<String, TypeInterpretation>
typealias Vars = Map<String, Definition<*>>
typealias Assumptions = Map<String, Interpretation>
typealias AllocQueue = List<BytesToAllocate>

data class OkayParseState<out R>(
    val result: R,
    val types: Types,
    val vars: Vars,
    val assumptions: Assumptions,
    val allocQueue: AllocQueue,
    override val target: String,
    override val index: Int,
) : ParseState<R>() {
    override fun toString() = """
        {
            "result": $result,
            "types": $types,
            "vars": $vars
            "index": $index
        }
    """.trimIndent()
}

data class ErroredParseState(
    val error: String,
    override val target: String,
    override val index: Int,
) : ParseState<Nothing>() {
    override fun toString() = """
        {
            "error": $error,
            "index": $index
        }
    """.trimIndent()
}

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
fun <R> UnitParseState(target: String): OkayParseState<R> {
    return OkayParseState(null as R, emptyMap(), emptyMap(), emptyMap(), emptyList(), target, 0)
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
    types: Types,
    vars: Vars,
    assumptions: Assumptions,
    allocQueue: AllocQueue,
    index: Int = state.index
): ParseState<R> {
    return OkayParseState(result, types, vars, assumptions, allocQueue, state.target, index)
}

fun <R> success(
    state: OkayParseState<*>,
    result: R,
    types: Types = state.types,
    vars: Vars = state.vars,
    assumptions: Assumptions = state.assumptions,
    allocQueue: AllocQueue = state.allocQueue,
    index: Int = state.index
): ParseState<R> {
    return OkayParseState(result, types, vars, assumptions, allocQueue, state.target, index)
}

fun <R> success(state: OkayParseState<R>): ParseState<R> {
    return state
}

fun ParseState<*>.subtarget(): String {
    return target.substring(index)
}
