package org.toptobes.parsercombinator

import org.toptobes.lang.utils.prettyString
import org.toptobes.parsercombinator.impls.crash
import org.toptobes.parsercombinator.impls.fail
import org.toptobes.parsercombinator.impls.succeed

class Parser<out R>(val parse: (OkayParseState<*>) -> ParseState<R>) {
    fun parsePropagating(oldState: ParseState<*>): ParseState<R> {
        if (oldState.isErrored()) {
            return oldState
        }

        return parse(oldState)
    }

    operator fun invoke(target: String, symbols: SymbolMap): ParseState<R> {
        return parsePropagating(UnitParseState<R>(target, symbols))
    }

    fun log(target: String, symbols: SymbolMap, logFn: (Any) -> Unit = ::println): ParseState<R> {
        return invoke(target, symbols).also {
            logFn(it.prettyString())
        }
    }

    fun <R2> map(mapper: (R) -> R2) = Parser { oldState ->
        val nextState = parsePropagating(oldState)

        if (nextState.isErrored()) {
            return@Parser errored(nextState)
        }

        return@Parser success(nextState, mapper(nextState.result))
    }

    fun <R2> flatMap(generator: (R) -> Parser<R2>) = Parser { oldState ->
        val nextState = parsePropagating(oldState)

        if (nextState.isErrored()) {
            return@Parser errored(nextState)
        }

        return@Parser generator(nextState.result).parsePropagating(nextState)
    }

    fun require(msg: String = "", crashing: Boolean = false, predicate: (R) -> Boolean) = flatMap {
        if (!predicate(it)) {
            return@flatMap if (crashing) crash(msg) else fail(msg)
        }
        return@flatMap succeed(it)
    }
}
