package org.toptobes.parsercombinator

import org.toptobes.lang.utils.StatefulParsingException
import org.toptobes.lang.utils.StatelessParsingException

class Context<T>(initialState: ParseState<T, *>) {
    var state: ParseState<T, *> = initialState

    infix fun <R> parse(parser: Parser<T, R>): R? {
        val nextState = parser.parsePropagating(state)
        state = nextState
        return nextState.result
    }

    infix fun <R> peek(parser: Parser<T, R>): R? {
        val nextState = parser.parsePropagating(state)
        return nextState.result
    }

    infix fun <R> canPeek(parser: Parser<T, R>): Boolean {
        return peek(parser) != null
    }

    infix fun <R> tryParse(parser: Parser<T, R>): R? {
        val nextState = parser.parsePropagating(state)

        if (nextState.isOkay) {
            state = nextState
        }
        return nextState.result
    }

    inline fun <R, R2> tryParse(parser: Parser<T, R>, block: (R) -> R2): R2? {
        return tryParse(parser)?.let(block)
    }

    infix fun <R> canTryParse(parser: Parser<T, R>): Boolean {
        return tryParse(parser) != null
    }
}

// I know this is terrible practice but whatever this is for myself so who cares
private class ContextualParseSuccess(val result: Any?) : Exception()
private class ContextualParseError(val errorInf: ErrorResult) : Exception()

class ContextScope<R>(private val ctx: Context<String>) {
    fun fail(err: String): Nothing = fail(object : ErrorResult {
        override fun toString() = err
    })

    fun fail(err: ErrorResult): Nothing {
        throw ContextualParseError(err)
    }

    fun crash(msg: String): Nothing {
        throw StatefulParsingException(msg, ctx.state)
    }

    fun success(data: R): Nothing {
        throw ContextualParseSuccess(data)
    }

    fun cfail(err: String): () -> Nothing {
        return { fail(err) }
    }

    fun ccrash(err: String): () -> Nothing {
        return { crash(err) }
    }

    inline infix fun <T> T?.or(nothing: () -> Nothing): T {
        return this ?: nothing()
    }
}

class contextual<R>(val fn: ContextScope<R>.(Context<String>) -> Nothing) : Parser<String, R>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out R> {
        val context = Context(oldState)

        try {
            ContextScope<R>(context).fn(context)
        } catch (e: ContextualParseSuccess) {
            return success(context.state, e.result as R)
        } catch (e: ContextualParseError) {
            return errored(context.state, e.errorInf)
        } catch (e: StatelessParsingException) {
            throw StatefulParsingException(e.message!!, context.state)
        }
    }
}
