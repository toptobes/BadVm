package org.toptobes.lang.ast

import org.toptobes.lang.parsing.instructions

data class Instruction(
    val mnemonic: String,
    val operands: List<Operand>,
) : AstNode {
    val metadata = getTag()
        .let { tag ->
            instructions[mnemonic]!!.first { it.tag == tag }
        }
}

fun Instruction.getTag(): String {
    val mnemonic = mnemonic.uppercase()

    val args = operands
        .reversed()
        .joinToString(separator = "_") { it.operandAssociation }

    return (mnemonic + "_" + args)
        .trimEnd('_')
}
