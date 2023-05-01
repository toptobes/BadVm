package org.toptobes.lang2.nodes

import org.toptobes.lang2.utils.Word

data class Imm16  (override val value:   Word) : WordOperand
data class Imm8   (override val value:   Byte) : ByteOperand
data class ImmAddr(override val address: Word) : WordAddrOperand