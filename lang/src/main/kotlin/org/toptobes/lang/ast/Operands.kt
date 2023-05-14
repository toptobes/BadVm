package org.toptobes.lang.ast

import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.reg16Codes
import org.toptobes.lang.utils.reg8Codes
import org.toptobes.lang.utils.toBytes

sealed interface Operand : AstNode {
    val operandAssociation: String
    val bytes: ByteArray
}

// -- IMMs/MEMs --

data class Imm16(val value: Word) : Operand {
    override val operandAssociation get() = "IMM16"
    override val bytes get() = value.toBytes()
}

data class Imm8 (val value: Byte) : Operand {
    override val operandAssociation get() = "IMM8"
    override val bytes get() = value.toBytes()
}

data class Mem16(val addr: Word) : Operand {
    override val operandAssociation get() = "MEM16"
    override val bytes get() = addr.toBytes()
}

data class Mem8 (val addr: Word) : Operand {
    override val operandAssociation get() = "MEM8"
    override val bytes get() = addr.toBytes()
}

// -- REGISTERS --

sealed interface RegisterOperand : Operand {
    val code: Byte
}

data class Reg16(val regName: String) : RegisterOperand {
    override val code = reg16Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "REG16"
    override val bytes get() = code.toBytes()
}

data class Reg8(val regName: String) : RegisterOperand {
    override val code = reg8Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "REG8"
    override val bytes get() = code.toBytes()
}

data class RegPtr(val regName: String) : RegisterOperand {
    override val code = reg16Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "PTR"
    override val bytes get() = code.toBytes()
}

private fun throwInvalidRegister(regName: String): Nothing {
    throw IllegalArgumentException("Invalid register $regName")
}
