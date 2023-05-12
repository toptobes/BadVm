package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.lang.utils.UWord
import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

val identifier = sequence(
    regex("[_a-zA-Z]"),
    regex("\\w+") withDefault "",
).map { it[0] + it[1] }

private val hexadecimal = any(
    regex("0x([a-fA-F0-9]+)", 1),
    regex("([a-fA-F0-9]+)h" , 1),
)..{ it.toInt(16) }

private val binary = any(
    regex("0b([10]+)", 1),
    regex("([10]+)b" , 1),
)..{ it.toInt(2) }

private val charCode = betweenSingleQuotes(char)
    .map { it.code }

private val decimal = digits
    .map { it.toInt() }

private val signMulti = (!(str("-") or str("+")))
    .map { if (it == "-") -1 else 1 }

private val number = (signMulti then any(
    hexadecimal, binary,
    charCode, decimal
))..{ it[0] * it[1] }

val word = number
    .flatMap { when {
        it in Word.MIN_VALUE..0 -> succeed(it.toWord())
        it.toUInt() in 0u..UWord.MAX_VALUE.toUInt() -> succeed(it.toWord())
        else -> fail("word '$number' !in word.min..uword.max")
    }}

val byte = number
    .flatMap { when {
        it in Byte.MIN_VALUE..0 -> succeed(it.toByte())
        it.toUInt() in 0u..UByte.MAX_VALUE.toUInt() -> succeed(it.toByte())
        else -> fail("byte '$number' !in byte.min..ubyte.max")
    }}

fun <R> cStyleArrayOf(parser: Parser<R>): Parser<List<R>> {
    return betweenCurlyBrackets(sepByCommas(parser))
}

inline fun <reified T> symbol(crashing: Boolean = false) = symbol.flatMap {
    if (it.first !is T) {
        if (crashing) crash("${it::class.simpleName} is not ${T::class.simpleName}")
    }
    succeed(it)
}

val symbol = contextual {
    val names = ctx parse -sepByPeriods(-identifier) orFail "Not a symbol"

    val firstSymbol = ctx.state.vars[names[0]] orCrash "${names[0]} is not a valid identifier"

    val (interpretation, resolved) = when (val interpretation = ctx.state.assumptions[names[0]]!!) {
        is TypeInterpretation -> resolveField(interpretation, names.drop(1))
        is Ptr -> WordInterpretation to firstSymbol.bytes
        else -> interpretation to firstSymbol.bytes
    }

    succeed(interpretation to resolved)
}

private fun resolveField(type: TypeInterpretation, fieldNames: List<String>): Pair<Interpretation, ByteArray> {
    val field = type.fields[fieldNames[0]]
        ?: throw ParsingException("${fieldNames[0]} is not a valid field")

    return when (val interpretation = field.interpretation) {
        is TypeInterpretation -> resolveField(interpretation, fieldNames.drop(1))
        is Ptr -> WordInterpretation to field.bytes
        else -> interpretation to field.bytes
    }
}
