package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.constructors.singleByte
import org.toptobes.lang.parsing.constructors.singleWord
import org.toptobes.lang.utils.reg16Codes
import org.toptobes.lang.utils.reg8Codes
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.impls.*
import org.toptobes.parsercombinator.rangeTo

val reg16 = any(reg16Codes.keys.map(::str))..(::Reg16)
val reg8  = any(reg8Codes .keys.map(::str))..(::Reg8)
val ptr   = betweenSquareBrackets(reg16.map { RegPtr(it.regName) })

val imm16 = any(word, singleWord..{ it.toWord() })..(::Imm16)
val imm8  = any(byte, singleByte..{ it[0] })..(::Imm8)

val mem16 = mem.require { it.first.interpretation == WordInterpretation }..{ it.second.toWord() }..(::Mem16)
val mem8  = mem.require { it.first.interpretation == ByteInterpretation }..{ it.second.toWord() }..(::Mem8)

val operandParserMap = mapOf(
    "REG16" to reg16,
    "REG8"  to reg8,
    "PTR"   to ptr,
    "IMM8"  to imm8,
    "IMM16" to imm16,
    "MEM16" to mem16,
    "MEM8"  to mem8,
)
