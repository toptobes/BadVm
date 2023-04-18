package org.toptobes.parsercombinator

import org.toptobes.lang.utils.Either

data class ParseState<T, R>(
    val result: R?,
    val target: T,
    val index: Int,
    val error: ErrorResult? = null
)

abstract class Parser<T, out R> {
    abstract fun parse(oldState: ParseState<T, *>): ParseState<T, out R>

    fun parsePropagating(oldState: ParseState<T, *>): ParseState<T, out R> {
        if (oldState.isErrored) {
            return errored(oldState)
        }

        return parse(oldState)
    }

    operator fun invoke(target: T): ParseState<T, out R> {
        return parsePropagating(ParseState(null, target, 0))
    }

    fun log(target: T, logFn: (Any) -> Unit = ::println): ParseState<T, out R> {
        return invoke(target).also {
            if (it.isErrored) logFn(it.error?.rootCause()!!) else logFn(it.result!!)
        }
    }

    fun <R2> map(mapper: (R) -> R2): Parser<T, R2> = object : Parser<T, R2>() {
        override fun parse(oldState: ParseState<T, *>): ParseState<T, R2> {
            val nextState = this@Parser.parsePropagating(oldState)

            if (nextState.isErrored) {
                return errored(nextState)
            }

            return success(nextState, mapper(nextState.result!!))
        }
    }

    fun errorMap(mapper: (ErrorResult) -> ErrorResult): Parser<T, R> = object : Parser<T, R>() {
        override fun parse(oldState: ParseState<T, *>): ParseState<T, out R> {
            val nextState = this@Parser.parsePropagating(oldState)

            if (nextState.isOkay) {
                return nextState
            }

            return errored(nextState, mapper(nextState.error!!))
        }
    }

    fun basicErrorMap(mapper: (ErrorResult) -> String): Parser<T, R> = errorMap { BasicErrorResult(mapper(it)) }

    fun crashOnErr(mapper: (ErrorResult) -> String): Parser<T, R> = object : Parser<T, R>() {
        override fun parse(oldState: ParseState<T, *>): ParseState<T, out R> {
            val nextState = this@Parser.parsePropagating(oldState)

            if (nextState.isOkay) {
                return nextState
            }

            throw IllegalStateException(mapper(nextState.error!!))
        }
    }

    fun <R2> chain(generator: (R) -> Parser<T, R2>): Parser<T, R2> = object : Parser<T, R2>() {
        override fun parse(oldState: ParseState<T, *>): ParseState<T, out R2> {
            val nextState = this@Parser.parsePropagating(oldState)

            if (nextState.isErrored) {
                return errored(nextState)
            }

            return generator(nextState.result!!).parsePropagating(nextState)
        }
    }
}

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
    fun fail(err: String): Nothing {
        throw ContextualParseError(object : ErrorResult {
            override fun toString() = err
        })
    }

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

fun <T, E, R> contextuallyParse(target: T, fn: ContextScope<E, R>.(Context<T>) -> Nothing): Pair<Either<E, R>, ParseState<T, *>> {
    val context = Context(ParseState(null, target, 0))

    try {
        ContextScope<E, R>().fn(context)
    } catch (e: ContextualParseSuccess) {
        return Either.Right(e.result as R) to context.state
    } catch (e: ContextualParseError) {
        return Either.Left(e.errorInf as E) to context.state
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
