package org.toptobes.parsercombinator

import org.toptobes.RESERVED_MEM_SIZE
import org.toptobes.lang.ast.*
import org.toptobes.lang.utils.toBytes

class Context(initialState: OkayParseState<*>) {
    var state: OkayParseState<*> = initialState
    var errorStr: String? = null

    infix fun <R> peek(parser: Parser<R>): R? {
        val nextState = parser.parsePropagating(state)
        return if (nextState.isOkay()) nextState.result else null
    }

    infix fun <R> canPeek(parser: Parser<R>): Boolean {
        return peek(parser) != null
    }

    infix fun <R> parse(parser: Parser<R>): R? {
        val nextState = parser.parsePropagating(state)

        if (nextState.isOkay()) {
            state = nextState
            return nextState.result
        }

        errorStr = nextState.error
        return null
    }

    inline fun <R, R2> parse(parser: Parser<R>, block: (R) -> R2): R2? {
        return parse(parser)?.let(block)
    }

    infix fun <R> canParse(parser: Parser<R>): Boolean {
        return parse(parser) != null
    }

    fun addVar(variable: Variable, alloc: Boolean) {
        val actualVar = if (alloc) {
            val newAddress = state.allocations.size + RESERVED_MEM_SIZE

            val newAllocation = state.allocations + variable.bytes
            state = state.copy(allocations = newAllocation)

            variable.copy(bytes = newAddress.toBytes())
        } else variable

        state = state.copy(symbols = state.symbols + actualVar)
    }

    fun addType(type: TypeIntrp) {
        state = state.copy(symbols = state.symbols + type)
    }

    fun addLabel(label: Label) {
        state = state.copy(symbols = state.symbols + label)
    }

    fun addMacro(macro: Macro) {
        state = state.copy(symbols = state.symbols + macro)
    }

    inline fun <reified T> lookup(name: String): T? {
        return state.symbols[name] as? T
    }

    private operator fun SymbolMap.plus(symbol: Symbol): Map<String, Symbol> {
        return this + (symbol.name to symbol)
    }
}

// I know this is terrible practice but whatever this is for myself so who cares
private class ContextualParseSuccess(val result: Any?) : Exception()
private class ContextualParseError(val errorMsg: String) : Exception()

class ContextScope<R>(val ctx: Context) {
    fun fail(str: String = ""): Nothing {
        throw ContextualParseError(str)
    }

    fun crash(msg: String): Nothing {
        throw ParsingException(msg)
    }

    fun succeed(data: R): Nothing {
        throw ContextualParseSuccess(data)
    }

    infix fun <T> T?.orFail(msg: String): T {
        return this ?: fail(msg)
    }

    infix fun <T> T?.orCrash(msg: String): T {
        return this ?: crash(msg)
    }
}

@Suppress("UNCHECKED_CAST")
fun <R> contextual(fn: ContextScope<R>.(Context) -> Nothing) = Parser { oldState ->
    val context = Context(oldState)

    try {
        ContextScope<R>(context).fn(context)
    } catch (e: ContextualParseSuccess) {
        if (context.state.isErrored()) {
            throw IllegalStateException("ContextualParseSuccess yet context is errored")
        }

        success(context.state, e.result as R)
    } catch (e: ContextualParseError) {
        errored(context.state, e.errorMsg)
    } catch (e: ParsingException) {
        throw DescriptiveParsingException(e.message!!, context.state)
    }
}
