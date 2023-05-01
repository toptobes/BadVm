package org.toptobes.lang.ast

import org.toptobes.lang.parsing.InstructionMetadata
import org.toptobes.lang.parsing.instructions

data class Instruction(
    val mnemonic: String,
    val args: List<Operand>
) : AstNode {
    override fun toString() = """
        { "$mnemonic": ${args.map { "\"${it.operandAssociation}\"" }} }
    """.trimIndent()

    val metadata: InstructionMetadata?
        get() {
            val mnemonic = mnemonic.uppercase()

            val args = args
                .reversed()
                .joinToString(separator = "_") { it.operandAssociation }

            val tag = (mnemonic + "_" + args)
                .trimEnd('_')

            return instructions[mnemonic]?.first { it.tag == tag }
        }
}
