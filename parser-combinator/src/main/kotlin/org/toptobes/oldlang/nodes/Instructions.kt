package org.toptobes.oldlang.nodes

data class Instruction(
    val mnemonic: String,
    val args: List<Operand>
) : Node {
    override fun toString() = """
        { "$mnemonic": ${args.map { "\"${it.operandAssociation}\"" }} }
    """.trimIndent()
}
