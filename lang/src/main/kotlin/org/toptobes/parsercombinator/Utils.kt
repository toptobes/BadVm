package org.toptobes.parsercombinator

import org.toptobes.parsercombinator.impls.*

infix fun <R> Parser<R>.or(other: Parser<R>) =
    any(this, other)

operator fun Parser<String>.not() =
    this withDefault ""

operator fun <R> Parser<R>.times(times: Int) =
    repeatTimes(this, times)

operator fun <R> Parser<R>.unaryPlus() =
    repeatedly(this)

operator fun <R, R2> Parser<R>.rangeTo(mapper: (R) -> R2) =
    this.map(mapper)

infix fun <R> Parser<R>.then(other: Parser<R>) =
    sequence(this, other)

operator fun <R> Parser<R>.unaryMinus() =
    between(whitespace withDefault "", this)
