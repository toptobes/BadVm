package org.toptobes.lang.parsers

import org.toptobes.lang.nodes.Pointer
import org.toptobes.lang.nodes.Reg16
import org.toptobes.lang.nodes.Reg8
import org.toptobes.lang.mappings.reg16Codes
import org.toptobes.lang.mappings.reg8Codes
import org.toptobes.lang.utils.VarDefs
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.between
import org.toptobes.parsercombinator.impls.str

fun imm16(vars: VarDefs) = any(pureImm16, wordVariable(vars), nullptr)
fun imm8 (vars: VarDefs) = any(pureImm8,  byteVariable(vars))

fun mem(vars: VarDefs) = any(
    memAddress(vars), wordVariable(vars),
    label
)

fun reg16(ignored: VarDefs) = any(*reg16Codes.keys.map { str(it) }.toTypedArray())
    .map(::Reg16)

fun reg8(ignored: VarDefs) = any(*reg8Codes.keys.map { str(it) }.toTypedArray())
    .map(::Reg8)

fun ptr(vars: VarDefs) = between.squareBrackets(reg16(vars))
    .map { Pointer(it.regName) }

val operandParserMap = mapOf(
    "REG16" to ::reg16,
    "REG8"  to ::reg8,
    "IMM16" to ::imm16,
    "IMM8"  to ::imm8,
    "MEM16" to ::mem,
//  "MEM8"  to ::mem,
    "PTR"   to ::ptr,
)
