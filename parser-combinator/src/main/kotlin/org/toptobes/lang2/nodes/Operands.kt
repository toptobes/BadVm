package org.toptobes.lang2.nodes

import org.toptobes.lang2.utils.Word

interface Operand : Node {
    val operandAssociation: String
}

sealed interface WordOperand : Operand {
    override val operandAssociation get() = "IMM16"
    val value: Word
}

sealed interface ByteOperand : Operand {
    override val operandAssociation get() = "IMM8"
    val value: Byte
}

sealed interface WordAddrOperand : Operand {
    override val operandAssociation get() = "MEM16"
    val address: Word
}

sealed interface ByteAddrOperand : Operand {
    override val operandAssociation get() = "MEM8"
    val address: Word
}
