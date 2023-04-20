package org.toptobes.parsercombinator

class Context<T>(initialState: ParseState<T, *>) {
    var state: ParseState<T, *> = initialState

    infix fun <R> parse(parser: Parser<T, R>): R? {
        val nextState = parser.parsePropagating(state)
        state = nextState
        return nextState.result
    }

    infix fun <R> tryParse(parser: Parser<T, R>): R? {
        val nextState = parser.parsePropagating(state)
        return nextState.result
    }

    infix fun <R> canParse(parser: Parser<T, R>): Boolean {
        return tryParse(parser) != null
    }

    inline fun <R, R2> ifParseable(parser: Parser<T, R>, block: (R) -> R2): R2? {
        val nextState = parser.parsePropagating(state)

        if (nextState.isOkay) {
            state = nextState
        }
        return nextState.result?.let(block)
    }
}

// I know this is terrible practice but whatever this is for myself so who cares
private class ContextualParseSuccess(val result: Any?) : Exception()
private class ContextualParseError(val errorInf: Any?) : Exception()

class ContextScope<E, R> {
    fun fail(err: String): Nothing = fail(object : ErrorResult {
        override fun toString() = err
    })

    fun fail(err: ErrorResult): Nothing {
        throw ContextualParseError(err)
    }

    fun crash(err: String): Nothing {
        throw IllegalStateException(err)
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

class contextual<T, R>(val fn: ContextScope<ErrorResult, R>.(Context<T>) -> Nothing) : Parser<T, R>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out R> {
        val context = Context(oldState)

        try {
            ContextScope<ErrorResult, R>().fn(context)
        } catch (e: ContextualParseSuccess) {
            return success(context.state, e.result as R)
        } catch (e: ContextualParseError) {
            return errored(context.state, e.errorInf as ErrorResult)
        }
    }
}
