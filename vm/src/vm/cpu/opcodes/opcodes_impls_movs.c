#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(mov16_reg_reg) {
        byte reg_dst = cpu_read_byte();
        byte reg_src = cpu_read_byte();
        cpu->registers[reg_dst] = cpu->registers[reg_src];
}

OPCODE_IMPL(mov16_imm_reg) {
        byte reg = cpu_read_byte();
        word imm = cpu_read_word();
        cpu->registers[reg] = imm;
}

OPCODE_IMPL(mov16_mem_reg) {
        byte reg = cpu_read_byte();
        word adr = cpu_read_word();
        cpu->registers[reg] = mem_read_word(cpu->mem, adr);
}

OPCODE_IMPL(mov16_reg_mem) {
        word adr = cpu_read_word();
        byte reg = cpu_read_byte();
        mem_write_word(cpu->mem, adr, cpu->registers[reg]);
}

void init_mov_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[mov16_reg_reg] = mov16_reg_reg_impl;
    cpu->opcodes[mov16_imm_reg] = mov16_imm_reg_impl;
    cpu->opcodes[mov16_mem_reg] = mov16_mem_reg_impl;
    cpu->opcodes[mov16_reg_mem] = mov16_reg_mem_impl;
}
