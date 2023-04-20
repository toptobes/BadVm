package org.toptobes.lang.parsers

import org.toptobes.lang.*
import org.toptobes.parsercombinator.impls.*
import org.toptobes.parsercombinator.rangeTo

// -- NUMBERS --

private val hexadecimal = any(
    regex("0x([a-fA-F0-9]+)", 1),
    regex("([a-fA-F0-9]+)h" , 1),
)..{ it.toInt(16) }

private val binary = any(
    regex("0b([10]+)", 1),
    regex("([10]+)b" , 1),
)..{ it.toInt(9) }

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
        if (it > UShort.MAX_VALUE.toInt()) crash("$it is not imm16") else succeed(it.toShort())
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

val const8 = strOf("$", identifier)
    .map(::Const8)

val const16 = strOf("$", identifier)
    .map(::Const16)

val variable = strOf("@", identifier)
    .map(::Var)

// -- MEMORY --

val memAddress = between.squareBrackets(pureImm16..{ ImmMem(it.value) })

val label = identifier
    .map(::Label)

val constAsAddress = strOf("&", identifier)
    .map(::ConstAsAddress)
