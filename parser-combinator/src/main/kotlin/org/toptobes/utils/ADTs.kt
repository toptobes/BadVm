package org.toptobes.utils

sealed interface Token
data class Operator  (val value: String)      : Token
data class Number    (val value: String)      : Token
data class Expression(val value: List<Token>) : Token

sealed class Either<out L, out R> {
    data class Left <out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}
