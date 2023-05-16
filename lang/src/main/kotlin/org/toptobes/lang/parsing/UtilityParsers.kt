package org.toptobes.lang.parsing

import org.toptobes.lang.ast.ByteIntrp
import org.toptobes.lang.ast.Interpretation
import org.toptobes.lang.ast.Ptr
import org.toptobes.lang.ast.WordIntrp
import org.toptobes.lang.utils.UWord
import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

val identifier = sequence(
    regex("[_a-zA-Z]"),
    regex("\\w*") withDefault "",
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

val castStart = contextual {
    ctx parse -str("<") orFail "Not a cast"
    val intrpName = ctx parse -identifier orCrash "Missing cast type"
    val isPtr = ctx canParse str("ptr")

    val intrp = when (intrpName) {
        "word", "dw" -> WordIntrp
        "byte", "db" -> ByteIntrp
        else -> ctx.lookup<Interpretation>(intrpName) ?: crash("$intrpName is not a valid interpretation")
    }

    if (isPtr) {
        succeed(Ptr(intrp))
    } else {
        succeed(intrp)
    }
}
