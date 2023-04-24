package org.toptobes.lang.nodes

import org.toptobes.lang.mappings.reg16Codes
import org.toptobes.lang.mappings.reg8Codes

interface Register : Operand {
    val code: Byte
}

data class Reg16(val regName: String) : Register {
    override val code: Byte = reg16Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "REG16"
}

data class Reg8(val regName: String) : Register {
    override val code: Byte = reg8Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "REG8"
}

data class Pointer(val regName: String) : Register {
    override val code: Byte = reg16Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "PTR"
}

private fun throwInvalidRegister(regName: String): Nothing {
    throw IllegalArgumentException("Invalid register $regName")
}
