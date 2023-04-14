#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(mov_reg16_reg16) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg16(dst) = cpu_reg16(src);
}

OPCODE_IMPL(mov_imm16_reg16) {
    byte reg = cpu_read_byte();
    word imm = cpu_read_word();
    cpu_reg16(reg) = imm;
}

OPCODE_IMPL(mov_mem_reg16) {
    byte reg = cpu_read_byte();
    word adr = cpu_read_word();
    cpu_reg16(reg) = mmap_read_word(cpu->mmap, adr);
}

OPCODE_IMPL(mov_reg16_mem) {
    word adr = cpu_read_word();
    byte reg = cpu_read_byte();
    mmap_write_word(cpu->mmap, adr, cpu_reg16(reg));
}

OPCODE_IMPL(mov_ptr_reg16) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg16(dst) = mmap_read_word(cpu->mmap, cpu->registers[src]);
}

OPCODE_IMPL(mov_reg8_reg8) {
    byte dst = cpu_read_byte();
    byte src = cpu_read_byte();
    cpu_reg8(dst) = mmap_read_word(cpu->mmap, ((byte*) cpu->registers)[src]);
}

OPCODE_IMPL(mov_imm8_reg8) {
    byte reg = cpu_read_byte();
    byte imm = cpu_read_byte();
    cpu_reg8(reg) = imm;
}

OPCODE_IMPL(mov_mem_reg8) {
    byte reg = cpu_read_byte();
    word adr = cpu_read_word();
    cpu_reg8(reg) = mmap_read_byte(cpu->mmap, adr);
}

OPCODE_IMPL(mov_reg8_mem) {
    word adr = cpu_read_word();
    byte reg = cpu_read_byte();
    mmap_write_byte(cpu->mmap, adr, cpu_reg8(reg));
}

void init_mov_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[mov_reg16_reg16] = mov_reg16_reg16_impl;
    cpu->opcodes[mov_imm16_reg16] = mov_imm16_reg16_impl;
    cpu->opcodes[mov_mem_reg16] = mov_mem_reg16_impl;
    cpu->opcodes[mov_reg16_mem] = mov_reg16_mem_impl;
    cpu->opcodes[mov_ptr_reg16] = mov_ptr_reg16_impl;
    cpu->opcodes[mov_reg8_reg8] = mov_reg8_reg8_impl;
    cpu->opcodes[mov_imm8_reg8] = mov_imm8_reg8_impl;
    cpu->opcodes[mov_mem_reg8] = mov_mem_reg8_impl;
    cpu->opcodes[mov_reg8_mem] = mov_reg8_mem_impl;
}
