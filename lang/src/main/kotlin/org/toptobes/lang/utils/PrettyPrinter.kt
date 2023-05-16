package org.toptobes.lang.utils

import org.toptobes.lang.ast.*
import org.toptobes.parsercombinator.ErroredParseState
import org.toptobes.parsercombinator.OkayParseState

fun Any.prettyString() = when (this) {
    is DeleteThisNode -> "*"
    is OkayParseState<*> -> okayParseStatePrettyString()
    is ErroredParseState -> errorParseStatePrettyString()
    is Interpretation -> interpretationPrettyString()
    is Instruction -> instructionPrettyString()
    is Symbol -> symbolPrettyString()
    is Operand -> operandPrettyString()
    else -> toString()
}

private fun OkayParseState<*>.okayParseStatePrettyString() = """
    {
        "result": ${(result as List<*>).astPrettyString()},
        "symbols": ${symbols.mapPrettyString()},
        "allocated": ${allocations.contentToString()},
        "index": $index
    }
""".trimIndent()

private fun ErroredParseState.errorParseStatePrettyString() = """
    {
        "error": $error,
        "index": $index
    }
"""

private fun Interpretation.interpretationPrettyString(): String = when (this) {
    is ByteIntrp -> "byte"
    is WordIntrp -> "word"
    is Ptr -> "ptr<${intrp.prettyString()}>"
    is Vec -> "vec<${intrp.prettyString()}>"
    is TypeIntrp -> """
        { name: $name, fields: ${fields.values.map(Field<*>::fieldPrettyString)} }
    """.trimIndent()
}

private fun Field<*>.fieldPrettyString(): String = when (intrp) {
    is TypeIntrp -> "${intrp.name} $name"
    else -> "${intrp.prettyString()} $name"
}

private fun Instruction.instructionPrettyString(): String {
    return "{ $mnemonic: ${operands.map(Any::prettyString)} }"
}

private fun Symbol.symbolPrettyString(): String = when (this) {
    is Label    -> "{ label: $name }"
    is Variable -> "{ name: $name, bytes: ${bytes.contentToString()}, intrp: ${intrp.prettyString()} }"
    is Macro -> "{ name: $name }"
    is TypeIntrp -> interpretationPrettyString()
}

private fun Operand.operandPrettyString() = when (this) {
    is Imm16  -> "{ op: $operandAssociation, val: $value }"
    is Imm8   -> "{ op: $operandAssociation, val: $value }"
    is Mem16  -> "{ op: $operandAssociation, val: $addr }"
    is Mem8   -> "{ op: $operandAssociation, val: $addr }"
    is Reg16  -> "{ op: $operandAssociation, val: $regName }"
    is Reg8   -> "{ op: $operandAssociation, val: $regName }"
    is RegPtr -> "{ op: $operandAssociation, val: $regName }"
    is Lbl    -> "{ op: $operandAssociation }"
}

private fun List<*>.astPrettyString(): String = this
    .filter { it != DeleteThisNode }
    .joinToString(",", "[",  "\n        ]") {
        "\n" + " ".repeat(12) + it?.prettyString()
    }

private fun Map<*, *>.mapPrettyString(): String = toList()
    .joinToString(",", "[",  "\n        ]") {
        "\n" + " ".repeat(12) + it.second?.prettyString()
    }
