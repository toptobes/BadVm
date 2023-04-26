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

interface WordAddrOperand : Operand {
    override val operandAssociation get() = "MEM16"
    val address: Word
}

interface ByteAddrOperand : Operand {
    override val operandAssociation get() = "MEM8"
    val address: Word
}
