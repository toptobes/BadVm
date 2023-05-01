package org.toptobes.lang.ast

data class Instruction(
    val mnemonic: String,
    val args: List<Operand>
) : AstNode {
    override fun toString() = """
        { "$mnemonic": ${args.map { "\"${it.operandAssociation}\"" }} }
    """.trimIndent()
}
