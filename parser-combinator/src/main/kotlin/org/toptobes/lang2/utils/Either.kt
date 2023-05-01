package org.toptobes.lang2.utils

sealed class Either<out L, out R> {
    data class Left <out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

inline fun <L, T> Either<L, *>.ifLeft(transformer: (L) -> T): T? {
    return if (this is Either.Left) transformer(value) else null
}

inline fun <R, T> Either<*, R>.ifRight(transformer: (R) -> T): T? {
    return if (this is Either.Right) transformer(value) else null
}
