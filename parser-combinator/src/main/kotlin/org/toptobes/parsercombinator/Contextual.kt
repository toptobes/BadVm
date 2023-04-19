package org.toptobes.parsercombinator

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
