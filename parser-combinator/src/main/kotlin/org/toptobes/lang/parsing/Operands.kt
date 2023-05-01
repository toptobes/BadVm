package org.toptobes.lang.parsing

import org.toptobes.lang.ast.Operand
import org.toptobes.lang.ast.Pointer
import org.toptobes.lang.ast.Reg16
import org.toptobes.lang.ast.Reg8
import org.toptobes.lang.utils.reg16Codes
import org.toptobes.lang.utils.reg8Codes
import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.betweenSquareBrackets
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.rangeTo

val reg16 = any(reg16Codes.keys.map(::str))..(::Reg16)
val reg8  = any(reg8Codes .keys.map(::str))..(::Reg8)
val ptr   = betweenSquareBrackets(reg16.map { Pointer(it.regName) })

val operandParserMap = mapOf<String, Parser<Operand>>(
    "REG16" to reg16,
    "REG8"  to reg8,
    "PTR"   to ptr,
//    "IMM8"  to ,
//    "IMM16" to ,
//    "MEM8"  to ,
//    "MEM16" to ,
)
