package org.toptobes.oldlang.nodes

import org.toptobes.oldlang.utils.Word

data class Imm16  (override val value:   Word) : WordOperand
data class Imm8   (override val value:   Byte) : ByteOperand
data class ImmAddr(override val address: Word) : WordAddrOperand
