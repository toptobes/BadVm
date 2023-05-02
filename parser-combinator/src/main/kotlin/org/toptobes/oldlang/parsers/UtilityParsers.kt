@file:Suppress("LocalVariableName")

package org.toptobes.oldlang.parsers

import org.toptobes.oldlang.nodes.*
import org.toptobes.oldlang.utils.UWord
import org.toptobes.parsercombinator.impls.*
import org.toptobes.parsercombinator.rangeTo
import org.toptobes.parsercombinator.unaryMinus

// -- NUMBERS --

private val hexadecimal = any(
    regex("0x([a-fA-F0-9]+)", 1),
    regex("([a-fA-F0-9]+)h" , 1),
)..{ it.toInt(16) }

private val binary = any(
    regex("0b([10]+)", 1),
    regex("([10]+)b" , 1),
)..{ it.toInt(2) }

private val charCode = between.singleQuotes(char)
    .map { it.code }

private val decimal = digits
    .map { it.toInt() }

private val number = any(
    hexadecimal, binary,
    charCode, decimal
)

val word = number
    .chain {
        if (it > UWord.MAX_VALUE.toInt()) crash("$it is not imm16") else succeed(it.toShort())
    }

val byte = number
    .chain {
        if (it > UByte.MAX_VALUE.toInt()) crash("$it is not imm8") else succeed(it.toByte())
    }

val pureImm16 = word..(::Imm16)
val pureImm8  = byte..(::Imm8)

// -- VARIABLES --

val identifier = sequence(
    regex("[_a-zA-Z]"),
    optionally(regex("\\w+"), ""),
).map { it[0] + it[1] }

// -- MEMORY --

fun memAddress(vars: Identifiables) = between.squareBrackets(imm16(vars)..{ ImmAddr(it.value) })

val label = identifier
    .map(::Label)

val nullptr = -str("?")
    .map { Imm16(0) }

// -- OTHER --

val equals = str("=")
