package org.toptobes.lang.nodes

import org.toptobes.lang.utils.Word

interface Operand : Node {
    val operandAssociation: String
}

interface WordOperand : Operand {
    override val operandAssociation get() = "IMM16"
    val value: Word
}

interface ByteOperand : Operand {
    override val operandAssociation get() = "IMM8"
    val value: Byte
}

interface AddrOperand : Operand {
    override val operandAssociation get() = "MEM"
    val address: Word
}
