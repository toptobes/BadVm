#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(add_reg16_reg16) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg16(dst) += cpu_reg16(src);
}

OPCODE_IMPL(add_imm16_reg16) {
    byte reg = cpu_read_byte();
    word imm = cpu_read_word();
    cpu_reg16(reg) += imm;
}

OPCODE_IMPL(sub_reg16_reg16) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg16(dst) -= cpu_reg16(src);
}

OPCODE_IMPL(sub_imm16_reg16) {
    byte reg = cpu_read_byte();
    word imm = cpu_read_word();
    cpu_reg16(reg) -= imm;
}

OPCODE_IMPL(mul_reg16_reg16) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg16(dst) *= cpu_reg16(src);
}

OPCODE_IMPL(mul_imm16_reg16) {
    byte reg = cpu_read_byte();
    word imm = cpu_read_word();
    cpu_reg16(reg) *= imm;
}

OPCODE_IMPL(add_reg8_reg8) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg8(dst) += cpu_reg8(src);
}

OPCODE_IMPL(add_imm8_reg8) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg8(reg) += imm;
}

OPCODE_IMPL(sub_reg8_reg8) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg8(dst) -= cpu_reg8(src);
}

OPCODE_IMPL(sub_imm8_reg8) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg8(reg) -= imm;
}

OPCODE_IMPL(mul_reg8_reg8) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg8(dst) *= cpu_reg8(src);
}

OPCODE_IMPL(mul_imm8_reg8) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg8(reg) *= imm;
}

void init_math_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[add_reg16_reg16] = add_reg16_reg16_impl;
    cpu->opcodes[add_imm16_reg16] = add_imm16_reg16_impl;
    cpu->opcodes[sub_reg16_reg16] = sub_reg16_reg16_impl;
    cpu->opcodes[sub_imm16_reg16] = sub_imm16_reg16_impl;
    cpu->opcodes[mul_reg16_reg16] = mul_reg16_reg16_impl;
    cpu->opcodes[mul_imm16_reg16] = mul_imm16_reg16_impl;
    cpu->opcodes[add_reg8_reg8] = add_reg8_reg8_impl;
    cpu->opcodes[add_imm8_reg8] = add_imm8_reg8_impl;
    cpu->opcodes[sub_reg8_reg8] = sub_reg8_reg8_impl;
    cpu->opcodes[sub_imm8_reg8] = sub_imm8_reg8_impl;
    cpu->opcodes[mul_reg8_reg8] = mul_reg8_reg8_impl;
    cpu->opcodes[mul_imm8_reg8] = mul_imm8_reg8_impl;
}
