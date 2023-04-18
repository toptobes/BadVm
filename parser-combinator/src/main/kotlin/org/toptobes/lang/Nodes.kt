package org.toptobes.lang

interface Node

// -- INSTRUCTIONS --

data class Instruction(
    val mnemonic: String,
    val args: List<Operand>
) : Node

// -- OTHER SUB-INTERFACES --

interface Definition : Node {
    val identifier: String
}

interface Operand : Node {
    val operandAssociation: String
    val name: String
}

interface Identifiable {
    val identifier: String
}

interface Identifiable16 : Identifiable {
    var actualValue: Short
}

interface Identifiable8  : Identifiable {
    var actualValue: Byte
}

// -- IMMEDIATES --

interface Immediate16 : Operand {
    val value: Short
}

interface Immediate8 : Operand {
    val value: Byte
}

data class Imm16(override val value: Short): Immediate16 {
    override val operandAssociation = "IMM16"
    override val name = "IMM16"
}

data class Imm8(override val value: Byte): Immediate8 {
    override val operandAssociation = "IMM8"
    override val name = "IMM8"
}

data class Const16(override val identifier: String): Immediate16, Identifiable16 {
    override var actualValue: Short = -1
    override val value get() = actualValue
    override val operandAssociation = "IMM16"
    override val name = "CONST16"
}

data class Const8(override val identifier: String): Immediate8, Identifiable8 {
    override var actualValue: Byte = -1
    override val value get() = actualValue
    override val operandAssociation = "IMM8"
    override val name = "CONST8"
}

data class Var(override val identifier: String) : Immediate16, Identifiable16 {
    override var actualValue: Short = -1
    override val value get() = actualValue
    override val operandAssociation = "IMM16"
    override val name = "VAR"
}

// -- REGISTERS --

interface Register : Operand {
    val code: Byte
}

data class Reg16(val regName: String) : Register {
    override val code: Byte = reg16Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "REG16"
    override val name = "REG16"
}

data class Reg8(val regName: String) : Register {
    override val code: Byte = reg8Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "REG8"
    override val name = "REG8"
}

data class Pointer(val regName: String) : Register {
    override val code: Byte = reg16Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "PTR"
    override val name = "PTR"
}

private fun throwInvalidRegister(regName: String): Nothing {
    throw IllegalArgumentException("Invalid register $regName")
}

// -- MEMORY --

interface MemAddress : Operand {
    val address: Short
}

data class ImmMem(val address: Short) : Operand {
    override val operandAssociation = "MEM"
    override val name = "IMM_MEM"
}

data class ConstAsAddress(override val identifier: String): MemAddress, Identifiable16 {
    override var actualValue: Short = -1
    override val address get() = actualValue
    override val operandAssociation = "MEM"
    override val name = "CONST16"
}

data class Label(override val identifier: String) : MemAddress, Identifiable16  {
    override var actualValue: Short = -1
    override val address get() = actualValue
    override val operandAssociation = "MEM"
    override val name = "LABEL"
}

// -- DEFINITIONS --

data class LabelDefinition(override val identifier: String) : Definition

data class Const8Definition(override val identifier: String, val byte: Byte) : Definition

data class Const16Definition(override val identifier: String, val word: Short) : Definition

data class Var8Definition(override val identifier: String, val bytes: List<Byte>) : Definition

data class Var16Definition(override val identifier: String, val words: List<Short>) : Definition
