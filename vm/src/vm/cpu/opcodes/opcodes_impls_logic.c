#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(shr_reg16_imm8) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) = ((word) cpu_reg16(reg)) >> imm;
}

OPCODE_IMPL(shr_reg16_reg8) {
    byte reg1 = cpu_read_byte();
    byte reg2 = cpu_read_byte();
    cpu_reg16(reg1) = ((word) cpu_reg16(reg1)) >> cpu_reg8(reg2);
}

OPCODE_IMPL(shl_reg16_imm8) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) <<= imm;
}

OPCODE_IMPL(shl_reg16_reg8) {
    byte reg1 = cpu_read_byte();
    byte reg2 = cpu_read_byte();
    cpu_reg16(reg1) <<= cpu_reg8(reg2);
}

OPCODE_IMPL(sar_reg16_imm8) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) = ((sword) cpu_reg16(reg)) >> ((sbyte) imm);
}

OPCODE_IMPL(sar_reg16_reg8) {
    byte reg1 = cpu_read_byte();
    byte reg2 = cpu_read_byte();
    cpu_reg16(reg1) = ((sword) cpu_reg16(reg1)) >> ((sbyte) cpu_reg8(reg2));
}

OPCODE_IMPL(and_reg16_reg16) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) &= imm;
}

OPCODE_IMPL(and_reg16_imm16) {
    byte src = cpu_read_byte();
    byte dst = cpu_read_byte();
    cpu_reg16(dst) &= cpu_reg16(src);
}

OPCODE_IMPL(xor_reg16_reg16) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) ^= imm;
}

OPCODE_IMPL(xor_reg16_imm16) {
    byte src = cpu_read_byte();
    byte dst = cpu_read_byte();
    cpu_reg16(dst) ^= cpu_reg16(src);
}

OPCODE_IMPL(or_reg16_reg16) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg16(reg) |= imm;
}

OPCODE_IMPL(or_reg16_imm16) {
    byte src = cpu_read_byte();
    byte dst = cpu_read_byte();
    cpu_reg16(dst) |= cpu_reg16(src);
}

OPCODE_IMPL(not_reg16) {
    byte reg = cpu_read_byte();
    cpu_reg16(reg) = ~cpu_reg16(reg);
}

void init_logic_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[shr_reg16_imm8] = shr_reg16_imm8_impl;
    cpu->opcodes[shr_reg16_reg8] = shr_reg16_reg8_impl;
    cpu->opcodes[shl_reg16_imm8] = shl_reg16_imm8_impl;
    cpu->opcodes[shl_reg16_reg8] = shl_reg16_reg8_impl;
    cpu->opcodes[sar_reg16_imm8] = sar_reg16_imm8_impl;
    cpu->opcodes[sar_reg16_reg8] = sar_reg16_reg8_impl;
    cpu->opcodes[and_reg16_reg16] = and_reg16_reg16_impl;
    cpu->opcodes[and_reg16_imm16] = and_reg16_imm16_impl;
    cpu->opcodes[xor_reg16_reg16] = xor_reg16_reg16_impl;
    cpu->opcodes[xor_reg16_imm16] = xor_reg16_imm16_impl;
    cpu->opcodes[or_reg16_reg16] = or_reg16_reg16_impl;
    cpu->opcodes[or_reg16_imm16] = or_reg16_imm16_impl;
    cpu->opcodes[not_reg16] = not_reg16_impl;
}
