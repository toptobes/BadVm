package org.toptobes.oldlang.parsers

import org.toptobes.oldlang.mappings.reg16Codes
import org.toptobes.oldlang.mappings.reg8Codes
import org.toptobes.oldlang.nodes.*
import org.toptobes.parsercombinator.impls.*
import org.toptobes.parsercombinator.rangeTo
import org.toptobes.varUsage

fun imm16(vars: Identifiables) = any(pureImm16, /*wordVariable(vars),*/ nullptr)
fun imm8 (vars: Identifiables) = any(pureImm8,  /*byteVariable(vars)*/)

fun mem16(vars: Identifiables) = any(
    varUsage(vars).chain { when(it) {
        is TypeAddrVariable -> succeed(TypeAddrVariable(it.identifier, it.to))
        is WordAddrVariable -> succeed(TypeAddrVariable(it.identifier, it.to))
        else -> crash("Not mem16")
    }},
    label,
    between.squareBrackets(word)..{ ImmAddr(it) },
)

fun mem8(vars: Identifiables) = any(
    varUsage(vars).chain { when(it) {
        is ByteAddrVariable -> succeed(ByteAddrVariable(it.identifier, it.to))
        else -> crash("Not mem8")
    }},
    between.squareBrackets(word)..{ ImmAddr(it) },
)

fun reg16(ignored: Identifiables) = any(*reg16Codes.keys.map { str(it) }.toTypedArray())
    .map(::Reg16)

fun reg8(ignored: Identifiables) = any(*reg8Codes.keys.map { str(it) }.toTypedArray())
    .map(::Reg8)

fun ptr(vars: Identifiables) = between.squareBrackets(reg16(vars))
    .map { Pointer(it.regName) }

val operandParserMap = mapOf(
    "REG16" to ::reg16,
    "REG8"  to ::reg8,
    "IMM16" to ::imm16,
    "IMM8"  to ::imm8,
    "MEM16" to ::mem16,
    "MEM8"  to ::mem8,
    "PTR"   to ::ptr,
)
