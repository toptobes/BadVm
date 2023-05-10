package org.toptobes.parsercombinator

import org.toptobes.lang.ast.TypeDefinition
import org.toptobes.lang.ast.VarDefinition
import org.toptobes.lang.ast.plus

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

    infix fun addVar(def: VarDefinition) {
        state = state.copy(vars = state.vars + def)
    }

    infix fun addType(def: TypeDefinition) {
        state = state.copy(types = state.types + def)
    }
}

// I know this is terrible practice but whatever this is for myself so who cares
private class ContextualParseSuccess(val result: Any?) : Exception()
private class ContextualParseError(val errorMsg: String) : Exception()

class ContextScope<R>(val ctx: Context) {
    fun fail(str: String): Nothing {
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
