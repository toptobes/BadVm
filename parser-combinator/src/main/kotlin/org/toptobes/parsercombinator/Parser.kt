package org.toptobes.parsercombinator

data class ParseState<R>(
    val result: R?,
    val target: String,
    val index: Int,
    val error: ErrorResult? = null
)

class Parser<out R>(val parse: (ParseState<*>) -> ParseState<out R>) {
    fun parsePropagating(oldState: ParseState<*>): ParseState<out R> {
        if (oldState.isErrored) {
            return errored(oldState)
        }

        return parse(oldState)
    }

    operator fun invoke(target: String): ParseState<out R> {
        return parsePropagating(ParseState(null, target, 0))
    }

    fun log(target: String, logFn: (Any) -> Unit = ::println): ParseState<out R> {
        return invoke(target).also {
            if (it.isErrored) logFn(it.error?.rootCause()!!) else logFn(it.result!!)
        }
    }

    fun <R2> map(mapper: (R) -> R2) = Parser { oldState ->
        val nextState = parsePropagating(oldState)

        if (nextState.isErrored) {
            return@Parser errored(nextState)
        }

        return@Parser succeed(nextState, mapper(nextState.result!!))
    }

    fun <R2> chain(generator: (R) -> Parser<R2>) = Parser { oldState ->
        val nextState = parsePropagating(oldState)

        if (nextState.isErrored) {
            return@Parser errored(nextState)
        }

        return@Parser generator(nextState.result!!).parsePropagating(nextState)
    }
}
