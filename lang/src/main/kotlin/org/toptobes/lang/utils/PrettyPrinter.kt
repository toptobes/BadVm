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
    else -> TODO("${this::class.simpleName} is not yet supported for prettifying")
}

private fun OkayParseState<*>.okayParseStatePrettyString() = """
    {
        "result": ${(result as List<*>).astPrettyString()},
        "types": ${types.mapPrettyString()},
        "vars": ${vars.mapPrettyString()},
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
    is ByteInterpretation -> "byte"
    is WordInterpretation -> "word"
    is Ptr -> "ptr<${interpretation.prettyString()}>"
    is Vec -> "vec<${interpretation.prettyString()}>"
    is TypeInterpretation -> """
        { name: $typeName, fields: ${fields.values.map(Field<*>::fieldPrettyString)} }
    """.trimIndent()
}

private fun Field<*>.fieldPrettyString(): String = when (interpretation) {
    is TypeInterpretation -> "${interpretation.typeName} $name"
    else -> "${interpretation.prettyString()} $name"
}

private fun Instruction.instructionPrettyString(): String {
    return "{ $mnemonic: ${operands.map(Any::prettyString)} }"
}

private fun Symbol.symbolPrettyString() = when (this) {
    is Constant -> "{ name: $name, bytes: ${bytes.contentToString()} }"
    is Label    -> "{ name: $name, addr: $address }"
    is Variable -> "{ name: $name, addr: $address, bytes: ${allocatedBytes.contentToString()} }"
}

private fun Operand.operandPrettyString() = when (this) {
    is Imm16  -> "{ op: $operandAssociation, val: $value }"
    is Imm8   -> "{ op: $operandAssociation, val: $value }"
    is Mem16  -> "{ op: $operandAssociation, val: $addr }"
    is Mem8   -> "{ op: $operandAssociation, val: $addr }"
    is Reg16  -> "{ op: $operandAssociation, val: $regName }"
    is Reg8   -> "{ op: $operandAssociation, val: $regName }"
    is RegPtr -> "{ op: $operandAssociation, val: $regName }"
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
