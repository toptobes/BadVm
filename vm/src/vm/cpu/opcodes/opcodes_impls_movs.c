#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(mov_reg16_reg16) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg16(dst) = cpu_reg16(src);
}

OPCODE_IMPL(mov_reg16_imm16) {
    byte reg = cpu_read_inst_byte();
    word imm = cpu_read_inst_word();
    cpu_reg16(reg) = imm;
}

OPCODE_IMPL(mov_reg16_mem16) {
    byte reg = cpu_read_inst_byte();
    word adr = cpu_read_inst_word();
    cpu_reg16(reg) = mmu_read_word(cpu->mmap, adr);
}

OPCODE_IMPL(mov_mem16_reg16) {
    word adr = cpu_read_inst_word();
    byte reg = cpu_read_inst_byte();
    mmu_write_word(cpu->mmap, adr, cpu_reg16(reg));
}

OPCODE_IMPL(mov_reg16_ptr) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg16(dst) = mmu_read_word(cpu->mmap, cpu_reg16(src));
}

OPCODE_IMPL(mov_ptr_reg16) {
    byte ptr = cpu_read_inst_byte();
    byte reg = cpu_read_inst_byte();
    mmu_write_word(cpu->mmap, cpu_reg16(ptr), cpu_reg16(reg));
}

OPCODE_IMPL(mov_ptr_imm16) {
    byte ptr = cpu_read_inst_byte();
    word imm = cpu_read_inst_word();
    mmu_write_word(cpu->mmap, cpu_reg16(ptr), imm);
}

OPCODE_IMPL(mov_reg16_ptr_imm16) {
    byte reg = cpu_read_inst_byte();
    byte ptr = cpu_read_inst_byte();
    word offset = cpu_read_inst_word();
    cpu_reg16(reg) = mmu_read_word(cpu->mmap, cpu_reg16(ptr) + offset);
}

OPCODE_IMPL(mov_ptr_imm16_reg16) {
    byte ptr = cpu_read_inst_byte();
    word offset = cpu_read_inst_word();
    byte reg = cpu_read_inst_byte();
    mmu_write_word(cpu->mmap, cpu_reg16(ptr) + offset, cpu_reg16(reg));
}

OPCODE_IMPL(mov_reg8_reg8) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg8(dst) = mmu_read_word(cpu->mmap, cpu_reg8(src));
}

OPCODE_IMPL(mov_reg8_imm8) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_byte();
    cpu_reg8(reg) = imm;
}

OPCODE_IMPL(mov_reg8_mem8) {
    byte reg = cpu_read_inst_byte();
    word adr = cpu_read_inst_word();
    cpu_reg8(reg) = mmu_read_byte(cpu->mmap, adr);
}

OPCODE_IMPL(mov_reg8_ptr) {
    byte dst = cpu_read_inst_byte();
    byte src = cpu_read_inst_byte();
    cpu_reg8(dst) = mmu_read_byte(cpu->mmap, cpu_reg8(src));
}

OPCODE_IMPL(mov_ptr_reg8) {
    byte ptr = cpu_read_inst_byte();
    byte reg = cpu_read_inst_byte();
    mmu_write_byte(cpu->mmap, cpu_reg8(ptr), cpu_reg8(reg));
}

OPCODE_IMPL(mov_ptr_imm8) {
    byte imm = cpu_read_inst_byte();
    byte ptr = cpu_read_inst_byte();
    mmu_write_byte(cpu->mmap, cpu_reg8(ptr), imm);
}

OPCODE_IMPL(mov_mem8_reg8) {
    word adr = cpu_read_inst_word();
    byte reg = cpu_read_inst_byte();
    mmu_write_byte(cpu->mmap, adr, cpu_reg8(reg));
}

void init_mov_opcodes(struct cpu_t *cpu) {
    ASSIGN_OPCODE(mov_reg16_reg16);
    ASSIGN_OPCODE(mov_reg16_imm16);
    ASSIGN_OPCODE(mov_reg16_ptr_imm16);
    ASSIGN_OPCODE(mov_ptr_imm16_reg16);
    ASSIGN_OPCODE(mov_reg16_mem16);
    ASSIGN_OPCODE(mov_mem16_reg16);
    ASSIGN_OPCODE(mov_reg16_ptr);
    ASSIGN_OPCODE(mov_ptr_reg16);
    ASSIGN_OPCODE(mov_ptr_imm16);
    ASSIGN_OPCODE(mov_reg8_reg8);
    ASSIGN_OPCODE(mov_reg8_imm8);
    ASSIGN_OPCODE(mov_mem8_reg8);
    ASSIGN_OPCODE(mov_ptr_reg8);
    ASSIGN_OPCODE(mov_reg8_ptr);
    ASSIGN_OPCODE(mov_ptr_imm8);
    ASSIGN_OPCODE(mov_mem8_reg8);
}
