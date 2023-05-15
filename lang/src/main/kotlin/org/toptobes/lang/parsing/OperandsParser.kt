package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.constructors.singleByte
import org.toptobes.lang.parsing.constructors.singleWord
import org.toptobes.lang.utils.reg16Codes
import org.toptobes.lang.utils.reg8Codes
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.impls.*
import org.toptobes.parsercombinator.rangeTo
import org.toptobes.parsercombinator.unaryMinus

val reg16 = any(reg16Codes.keys.map(::str))..(::Reg16)
val reg8  = any(reg8Codes .keys.map(::str))..(::Reg8)
val ptr   = betweenSquareBrackets(reg16.map { RegPtr(it.regName) })

val imm16 = singleWord..{ it.toWord() }..(::Imm16)
val imm8  = singleByte..{ it[0] }..(::Imm8)

val mem16 = mem.require { it.first.interpretation == WordInterpretation }..{ it.second.toWord() }..(::Mem16)
val mem8  = mem.require { it.first.interpretation == ByteInterpretation }..{ it.second.toWord() }..(::Mem8)

private val lbl = -label..{ Lbl(it.name) }

val operandParserMap = mapOf(
    "reg16" to reg16,
    "reg8"  to reg8,
    "ptr"   to ptr,
    "imm8"  to imm8,
    "imm16" to imm16,
    "mem8"  to mem8,
    "mem16" to any(mem16, lbl),
)
