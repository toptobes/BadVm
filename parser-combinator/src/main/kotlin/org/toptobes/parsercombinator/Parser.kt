package org.toptobes.parsercombinator

data class ParseState<T, R>(
    val result: R?,
    val target: T,
    val index: Int,
    val error: ErrorResult? = null
)

abstract class Parser<Target, out NewT> {
    abstract fun parse(oldState: ParseState<Target, *>): ParseState<Target, out NewT>

    fun parsePropagating(oldState: ParseState<Target, *>): ParseState<Target, out NewT> {
        if (oldState.isErrored) {
            return errored(oldState)
        }

        return parse(oldState)
    }

    operator fun invoke(target: Target): ParseState<Target, out NewT> {
        return parsePropagating(ParseState(null, target, 0))
    }

    fun log(target: Target, logFn: (Any) -> Unit = ::println): ParseState<Target, out NewT> {
        return invoke(target).also(logFn)
    }

    fun <MappedT> map(mapper: (NewT) -> MappedT): Parser<Target, MappedT> = object : Parser<Target, MappedT>() {
        override fun parse(oldState: ParseState<Target, *>): ParseState<Target, MappedT> {
            val nextState = this@Parser.parsePropagating(oldState)

            if (nextState.isErrored) {
                return errored(nextState)
            }

            return success(nextState, mapper(nextState.result!!))
        }
    }

    fun errorMap(mapper: (ErrorResult) -> ErrorResult): Parser<Target, NewT> = object : Parser<Target, NewT>() {
        override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out NewT> {
            val nextState = this@Parser.parsePropagating(oldState)

            if (nextState.isOkay) {
                return nextState
            }

            return errored(nextState, mapper(nextState.error!!))
        }
    }

    fun <ChainedT> chain(generator: (NewT) -> Parser<Target, ChainedT>): Parser<Target, ChainedT> = object : Parser<Target, ChainedT>() {
        override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out ChainedT> {
            val nextState = this@Parser.parsePropagating(oldState)

            if (nextState.isErrored) {
                return errored(nextState)
            }

            return generator(nextState.result!!).parsePropagating(nextState)
        }
    }
}
