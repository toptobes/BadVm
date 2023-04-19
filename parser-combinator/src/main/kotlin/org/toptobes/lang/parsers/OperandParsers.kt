package org.toptobes.lang.parsers

import org.toptobes.lang.*
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.between
import org.toptobes.parsercombinator.impls.str

val imm16 = any(pureImm16, const16)
val imm8  = any(pureImm8 , const8)

val mem = any(
    memAddress, variable,
    constAsAddress, label
)

val reg16 = any(*reg16Codes.keys.map { str(it) }.toTypedArray())
    .map(::Reg16)

val reg8 = any(*reg8Codes.keys.map { str(it) }.toTypedArray())
    .map(::Reg8)

val ptr = between.squareBrackets(reg16)
    .map { Pointer(it.regName) }

val operandParserMap = mapOf(
    "REG16" to reg16,
    "REG8"  to reg8,
    "IMM16" to imm16,
    "IMM8"  to imm8,
    "MEM"   to mem,
    "PTR"   to ptr,
)

