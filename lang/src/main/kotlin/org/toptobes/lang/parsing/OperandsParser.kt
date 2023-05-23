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

val reg16 = any(reg16Codes.keys.map(::str))..{ Reg16(it) to false }
val reg8  = any(reg8Codes .keys.map(::str))..{ Reg8(it)  to false }
val ptr   = sequence(-str("@"), any(reg16Codes.keys.map(::str)))..{ RegPtr(it[1]) to false }

val imm16 = singleWord..{ Mem16(it.first) to it.second }
val imm8  = singleByte..{ it[0] }..{ Imm8(it) to false }

val mem16 = mem.require { it.first.intrp == WordIntrp }..{ Mem16(it.second) to it.third }
val mem8  = mem.require { it.first.intrp == ByteIntrp }..{ Mem8(it.second)  to it.third }

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
