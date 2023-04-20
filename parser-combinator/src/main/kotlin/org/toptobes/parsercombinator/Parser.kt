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
