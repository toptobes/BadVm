package org.toptobes.parsercombinator

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class Context(initialState: ParseState<*>) {
    var state: ParseState<*> = initialState
        private set

    infix fun <R> parse(parser: Parser<R>): R? {
        val nextState = parser.parsePropagating(state)
        state = nextState
        return if (nextState.isOkay()) nextState.result else null
    }

    infix fun <R> peek(parser: Parser<R>): R? {
        val nextState = parser.parsePropagating(state)
        return if (nextState.isOkay()) nextState.result else null
    }

    infix fun <R> canPeek(parser: Parser<R>): Boolean {
        return peek(parser) != null
    }

    infix fun <R> tryParse(parser: Parser<R>): R? {
        val nextState = parser.parsePropagating(state)

        if (nextState.isOkay()) {
            state = nextState
            return nextState.result
        }
        return null
    }

    inline fun <R, R2> tryParse(parser: Parser<R>, block: (R) -> R2): R2? {
        return tryParse(parser)?.let(block)
    }

    infix fun <R> canTryParse(parser: Parser<R>): Boolean {
        return tryParse(parser) != null
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

    @ExperimentalContracts
    infix fun <T> T?.orFail(msg: String): T {
        return this ?: fail(msg)
    }

    @ExperimentalContracts
    infix fun <T> T?.orCrash(msg: String): T {
        return this ?: crash(msg)
    }
}

fun <R> contextual(fn: ContextScope<R>.(Context) -> Nothing) = Parser { oldState ->
    val context = Context(oldState)

    try {
        ContextScope<R>(context).fn(context)
    } catch (e: ContextualParseSuccess) {
        success(context.state, e.result as R)
    } catch (e: ContextualParseError) {
        errored(context.state, e.errorMsg)
    } catch (e: ParsingException) {
        throw DescriptiveParsingException(e.message!!, context.state)
    }
}
