package org.toptobes.lang.parsing

import org.toptobes.lang.utils.UWord
import org.toptobes.lang.utils.Word
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
    .chain {
        if (it.toUInt() <= UWord.MAX_VALUE) succeed(it.toShort()) else fail("number > uword.max_value")
    }

val byte = number
    .chain {
        if (it.toUInt() <= UByte.MAX_VALUE) succeed(it.toByte()) else fail("number > ubyte.max_value")
    }
