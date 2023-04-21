package org.toptobes.lang.nodes

data class Instruction(
    val mnemonic: String,
    val args: List<Operand>
) : Node

