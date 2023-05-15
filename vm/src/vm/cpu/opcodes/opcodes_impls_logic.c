#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(shr_imm8_reg16) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) = ((word) cpu_reg16(reg)) >> imm;
}

OPCODE_IMPL(shr_reg8_reg16) {
    byte reg1 = cpu_read_byte();
    byte reg2 = cpu_read_byte();
    cpu_reg16(reg1) = ((word) cpu_reg16(reg1)) >> cpu_reg8(reg2);
}

OPCODE_IMPL(shl_imm8_reg16) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) <<= imm;
}

OPCODE_IMPL(shl_reg8_reg16) {
    byte reg1 = cpu_read_byte();
    byte reg2 = cpu_read_byte();
    cpu_reg16(reg1) <<= cpu_reg8(reg2);
}

OPCODE_IMPL(sar_imm8_reg16) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) = ((sword) cpu_reg16(reg)) >> ((sbyte) imm);
}

OPCODE_IMPL(sar_reg8_reg16) {
    byte reg1 = cpu_read_byte();
    byte reg2 = cpu_read_byte();
    cpu_reg16(reg1) = ((sword) cpu_reg16(reg1)) >> ((sbyte) cpu_reg8(reg2));
}

OPCODE_IMPL(and_reg16_reg16) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg16(dst) &= cpu_reg16(src);
}

OPCODE_IMPL(and_imm16_reg16) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) &= imm;
}

OPCODE_IMPL(xor_reg16_reg16) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg16(dst) ^= cpu_reg16(src);
}

OPCODE_IMPL(xor_imm16_reg16) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) ^= imm;
}

OPCODE_IMPL(or_reg16_reg16) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg16(dst) |= cpu_reg16(src);
}

OPCODE_IMPL(or_imm16_reg16) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) |= imm;
}

OPCODE_IMPL(not_reg16) {
    byte reg = cpu_read_byte();
    cpu_reg16(reg) = ~cpu_reg16(reg);
}

void init_logic_opcodes(struct cpu_t *cpu) {
    ASSIGN_OPCODE(shr_imm8_reg16);
    ASSIGN_OPCODE(shr_reg8_reg16);
    ASSIGN_OPCODE(shl_imm8_reg16);
    ASSIGN_OPCODE(shl_reg8_reg16);
    ASSIGN_OPCODE(sar_imm8_reg16);
    ASSIGN_OPCODE(sar_reg8_reg16);
    ASSIGN_OPCODE(and_reg16_reg16);
    ASSIGN_OPCODE(and_imm16_reg16);
    ASSIGN_OPCODE(xor_reg16_reg16);
    ASSIGN_OPCODE(xor_imm16_reg16);
    ASSIGN_OPCODE(or_reg16_reg16);
    ASSIGN_OPCODE(or_imm16_reg16);
    ASSIGN_OPCODE(not_reg16);
}
