#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(shr_reg16_imm8) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_byte();
    cpu_reg16(reg) = ((word) cpu_reg16(reg)) >> imm;
}

OPCODE_IMPL(shr_reg16_reg8) {
    byte reg1 = cpu_read_inst_byte();
    byte reg2 = cpu_read_inst_byte();
    cpu_reg16(reg1) = ((word) cpu_reg16(reg1)) >> cpu_reg8(reg2);
}

OPCODE_IMPL(shl_reg16_imm8) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_byte();
    cpu_reg16(reg) <<= imm;
}

OPCODE_IMPL(shl_reg16_reg8) {
    byte reg1 = cpu_read_inst_byte();
    byte reg2 = cpu_read_inst_byte();
    cpu_reg16(reg1) <<= cpu_reg8(reg2);
}

OPCODE_IMPL(sar_reg16_imm8) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_byte();
    cpu_reg16(reg) = ((sword) cpu_reg16(reg)) >> ((sbyte) imm);
}

OPCODE_IMPL(sar_reg16_reg8) {
    byte reg1 = cpu_read_inst_byte();
    byte reg2 = cpu_read_inst_byte();
    cpu_reg16(reg1) = ((sword) cpu_reg16(reg1)) >> ((sbyte) cpu_reg8(reg2));
}

OPCODE_IMPL(and_reg16_reg16) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg16(dst) &= cpu_reg16(src);
}

OPCODE_IMPL(and_reg16_imm16) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_byte();
    cpu_reg16(reg) &= imm;
}

OPCODE_IMPL(xor_reg16_reg16) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg16(dst) ^= cpu_reg16(src);
}

OPCODE_IMPL(xor_reg16_imm16) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_byte();
    cpu_reg16(reg) ^= imm;
}

OPCODE_IMPL(or_reg16_reg16) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg16(dst) |= cpu_reg16(src);
}

OPCODE_IMPL(or_reg16_imm16) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_byte();
    cpu_reg16(reg) |= imm;
}

OPCODE_IMPL(not_reg16) {
    byte reg = cpu_read_inst_byte();
    cpu_reg16(reg) = ~cpu_reg16(reg);
}

void init_logic_opcodes(struct cpu_t *cpu) {
    ASSIGN_OPCODE(shr_reg16_imm8);
    ASSIGN_OPCODE(shr_reg16_reg8);
    ASSIGN_OPCODE(shl_reg16_imm8);
    ASSIGN_OPCODE(shl_reg16_reg8);
    ASSIGN_OPCODE(sar_reg16_imm8);
    ASSIGN_OPCODE(sar_reg16_reg8);
    ASSIGN_OPCODE(and_reg16_reg16);
    ASSIGN_OPCODE(and_reg16_imm16);
    ASSIGN_OPCODE(xor_reg16_reg16);
    ASSIGN_OPCODE(xor_reg16_imm16);
    ASSIGN_OPCODE(or_reg16_reg16);
    ASSIGN_OPCODE(or_reg16_imm16);
    ASSIGN_OPCODE(not_reg16);
}
