@file:Suppress("LocalVariableName")

package org.toptobes.lang.parsers

import org.toptobes.lang.nodes.*
import org.toptobes.lang.utils.VarDefs
import org.toptobes.parsercombinator.contextual
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

fun variable(vars: VarDefs) = contextual { ctx ->
    ctx tryParse -str("@")                                                             or cfail("Not a variable usage")

    val cascadingNames = ctx parse sepBy.periods(identifier, allowTrailingSep = false) or ccrash("No identifier found for variable usage")
    val firstName = cascadingNames[0]
    val first = vars[firstName]                                                        or ccrash("No identifier with name ${cascadingNames[0]}")

    val (_, variable) = cascadingNames.drop(1).fold(firstName to first) { (prevName, variable), name ->
        if (variable !is TypeInstance) {
            crash("Trying to call $name on non-defined-type $prevName")
        }

        val next = variable.fields.firstOrNull { it.identifier == name }               or ccrash("No identifier with name $name")
        next.identifier to next
    }

    success(variable)
}

fun wordVariable(vars: VarDefs) = variable(vars)
    .chain {
        if (it !is WordVarDefinition) {
            crash("not a word lol")
        } else {
            succeed(Imm16(it.word))
        }
    }

fun byteVariable(vars: VarDefs) = variable(vars)
    .chain {
        if (it !is ByteVarDefinition) {
            crash("not a byte lol")
        } else {
            succeed(Imm8(it.byte))
        }
    }

// -- MEMORY --

fun memAddress(vars: VarDefs) = between.squareBrackets(imm16(vars)..{ ImmAddr(it.value) })

val label = identifier
    .map(::Label)

val constAsAddress = strOf("&", identifier)
    .map(::AddrVariable)

val nullptr = -str("?")
    .map { Imm16(0) }

// -- OTHER --

val equals = str("=")
