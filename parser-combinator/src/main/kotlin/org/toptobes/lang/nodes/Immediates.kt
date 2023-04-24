package org.toptobes.lang.nodes

import org.toptobes.lang.utils.Word

data class Imm16  (override val value:   Word) : WordOperand
data class Imm8   (override val value:   Byte) : ByteOperand
data class ImmAddr(override val address: Word) : AddrOperand
