package org.toptobes.parsercombinator

class Parser<out R>(val parse: (ParseState<*>) -> ParseState<R>) {
    fun parsePropagating(oldState: ParseState<*>): ParseState<R> {
        if (oldState.isErrored()) {
            return oldState
        }

        return parse(oldState)
    }

    operator fun invoke(target: String): ParseState<R> {
        return parsePropagating(UnitParseState<R>(target))
    }

    fun log(target: String, logFn: (Any?) -> Unit = ::println): ParseState<R> {
        return invoke(target).also {
            if (it.isErrored()) logFn(it.error) else logFn(it.result)
        }
    }

    fun <R2> map(mapper: (R) -> R2) = Parser { oldState ->
        val nextState = parsePropagating(oldState)

        if (nextState.isErrored()) {
            return@Parser errored(nextState)
        }

        return@Parser success(nextState, mapper(nextState.result))
    }

    fun <R2> chain(generator: (R) -> Parser<R2>) = Parser { oldState ->
        val nextState = parsePropagating(oldState)

        if (nextState.isErrored()) {
            return@Parser errored(nextState)
        }

        return@Parser generator(nextState.result).parsePropagating(nextState)
    }
}
