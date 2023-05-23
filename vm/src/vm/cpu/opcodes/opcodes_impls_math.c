#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(add_reg16_reg16) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg16(dst) += cpu_reg16(src);
}

OPCODE_IMPL(add_reg16_imm16) {
    byte reg = cpu_read_inst_byte();
    word imm = cpu_read_inst_word();
    cpu_reg16(reg) += imm;
}

OPCODE_IMPL(sub_reg16_reg16) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg16(dst) -= cpu_reg16(src);
}

OPCODE_IMPL(sub_reg16_imm16) {
    byte reg = cpu_read_inst_byte();
    word imm = cpu_read_inst_word();
    cpu_reg16(reg) -= imm;
}

OPCODE_IMPL(mul_reg16_reg16) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg16(dst) *= cpu_reg16(src);
}

OPCODE_IMPL(mul_reg16_imm16) {
    byte reg = cpu_read_inst_byte();
    word imm = cpu_read_inst_word();
    cpu_reg16(reg) *= imm;
}

OPCODE_IMPL(inc_reg16) {
    byte reg = cpu_read_inst_byte();
    cpu_reg16(reg) += 1;
}

OPCODE_IMPL(dec_reg16) {
    byte reg = cpu_read_inst_byte();
    cpu_reg16(reg) -= 1;
}

OPCODE_IMPL(add_reg8_reg8) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg8(dst) += cpu_reg8(src);
}

OPCODE_IMPL(add_reg8_imm8) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_byte();
    cpu_reg8(reg) += imm;
}

OPCODE_IMPL(sub_reg8_reg8) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg8(dst) -= cpu_reg8(src);
}

OPCODE_IMPL(sub_reg8_imm8) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_byte();
    cpu_reg8(reg) -= imm;
}

OPCODE_IMPL(mul_reg8_reg8) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg8(dst) *= cpu_reg8(src);
}

OPCODE_IMPL(mul_reg8_imm8) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_byte();
    cpu_reg8(reg) *= imm;
}

OPCODE_IMPL(inc_reg8) {
    byte reg = cpu_read_inst_byte();
    cpu_reg8(reg) += 1;
}

OPCODE_IMPL(dec_reg8) {
    byte reg = cpu_read_inst_byte();
    cpu_reg8(reg) -= 1;
}

void init_math_opcodes(struct cpu_t *cpu) {
    ASSIGN_OPCODE(add_reg16_reg16);
    ASSIGN_OPCODE(add_reg16_imm16);
    ASSIGN_OPCODE(sub_reg16_reg16);
    ASSIGN_OPCODE(sub_reg16_imm16);
    ASSIGN_OPCODE(mul_reg16_reg16);
    ASSIGN_OPCODE(mul_reg16_imm16);
    ASSIGN_OPCODE(inc_reg16);
    ASSIGN_OPCODE(dec_reg16);
    ASSIGN_OPCODE(add_reg8_reg8);
    ASSIGN_OPCODE(add_reg8_imm8);
    ASSIGN_OPCODE(sub_reg8_reg8);
    ASSIGN_OPCODE(sub_reg8_imm8);
    ASSIGN_OPCODE(mul_reg8_reg8);
    ASSIGN_OPCODE(mul_reg8_imm8);
    ASSIGN_OPCODE(inc_reg8);
    ASSIGN_OPCODE(dec_reg8);
}
