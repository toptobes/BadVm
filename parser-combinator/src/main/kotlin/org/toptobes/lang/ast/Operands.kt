package org.toptobes.lang.ast

import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.reg16Codes
import org.toptobes.lang.utils.reg8Codes
import kotlin.properties.Delegates

interface SyntheticOperand : AstNode

interface Operand : SyntheticOperand {
    val operandAssociation: String
}

// -- IMMs/MEMs --

data class Imm16(val value: Word) : Operand {
    override val operandAssociation get() = "IMM16"
}

data class Imm8 (val value: Byte) : Operand {
    override val operandAssociation get() = "IMM8"
}

data class Mem16(val words: List<Word>) : Operand {
    var address by Delegates.notNull<Word>()
    override val operandAssociation get() = "MEM16"
}

data class Mem8 (val bytes: List<Byte>) : Operand {
    var address by Delegates.notNull<Word>()
    override val operandAssociation get() = "MEM8"
}

// -- REGISTERS --

sealed interface RegisterOperand : Operand {
    val code: Byte
}

data class Reg16(val regName: String) : RegisterOperand {
    override val code: Byte = reg16Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "REG16"
}

data class Reg8(val regName: String) : RegisterOperand {
    override val code: Byte = reg8Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "REG8"
}

data class Pointer(val regName: String) : RegisterOperand {
    override val code: Byte = reg16Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "PTR"
}

private fun throwInvalidRegister(regName: String): Nothing {
    throw IllegalArgumentException("Invalid register $regName")
}
