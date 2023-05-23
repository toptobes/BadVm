package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.lang.utils.UWord
import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

val identifierRegex = "[_a-zA-Z]\\w*"

val identifier = regex(identifierRegex)

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

val litWord = number
    .flatMap { when {
        it in Word.MIN_VALUE..0 -> succeed(it.toWord())
        it.toUInt() in 0u..UWord.MAX_VALUE.toUInt() -> succeed(it.toWord())
        else -> fail("word '$number' !in word.min..uword.max")
    }}

val litByte = number
    .flatMap { when {
        it in Byte.MIN_VALUE..0 -> succeed(it.toByte())
        it.toUInt() in 0u..UByte.MAX_VALUE.toUInt() -> succeed(it.toByte())
        else -> fail("byte '$number' !in byte.min..ubyte.max")
    }}

fun <R> cStyleArrayOf(parser: Parser<R>): Parser<List<R>> {
    return betweenCurlyBrackets(sepByCommas(parser))
}

inline fun <reified T : Interpretation> ContextScope<*>.intrp() = (ctx parse intrp)?.also {
    if (it !is T) {
        crash("Expected ${T::class.java.simpleName}, got ${it::class.java.simpleName}")
    }
} as T

val intrp = contextual {
    val intrpName = ctx parse identifier orFail "Not an intrp"
    val modifiers = ctx parse pool(-betweenSquareBrackets(litWord)..{ it.toString() }, -str("ptr")) orCrash ""

    val isPtr = "ptr" in modifiers

    if (isPtr && modifiers.size == 2) {
        crash("Can't have a pointer to a vector")
    }
    val isVec = modifiers.size == 1 && !isPtr

    val intrp = when (intrpName) {
        "word", "dw" -> WordIntrp
        "byte", "db" -> ByteIntrp
        else -> ctx.lookup<Interpretation>(intrpName) ?: crash("$intrpName is not a valid interpretation")
    }

    succeed(when {
        isPtr -> Ptr(intrp)
        isVec -> Vec(intrp, modifiers[0].toInt())
        else  -> intrp
    })
}
