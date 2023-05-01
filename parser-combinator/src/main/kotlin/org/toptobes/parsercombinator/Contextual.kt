package org.toptobes.parsercombinator

import org.toptobes.lang.utils.DescriptiveParsingException
import org.toptobes.lang.utils.ParsingException

class Context(initialState: ParseState<*>) {
    var state: ParseState<*> = initialState
        private set

    infix fun <R> parse(parser: Parser<R>): R? {
        val nextState = parser.parsePropagating(state)
        state = nextState
        return nextState.result
    }

    infix fun <R> peek(parser: Parser<R>): R? {
        val nextState = parser.parsePropagating(state)
        return nextState.result
    }

    infix fun <R> canPeek(parser: Parser<R>): Boolean {
        return peek(parser) != null
    }

    infix fun <R> tryParse(parser: Parser<R>): R? {
        val nextState = parser.parsePropagating(state)

        if (nextState.isOkay) {
            state = nextState
        }
        return nextState.result
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
private class ContextualParseError(val errorInf: ErrorResult) : Exception()

class ContextScope<R>(private val ctx: Context) {
    fun fail(str: String): Nothing {
        throw ContextualParseError(BasicErrorResult(str))
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

fun <R> contextual(fn: ContextScope<R>.(Context) -> Nothing) = Parser { oldState ->
    val context = Context(oldState)

    try {
        ContextScope<R>(context).fn(context)
    } catch (e: ContextualParseSuccess) {
        succeed(context.state, e.result as R)
    } catch (e: ContextualParseError) {
        errored<R>(context.state, e.errorInf)
    } catch (e: ParsingException) {
        throw DescriptiveParsingException(e.message!!, context.state)
    }
}
