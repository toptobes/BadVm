#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(add16_reg_reg) {
    byte reg_dst = cpu_read_byte();
    byte reg_src = cpu_read_byte();
    cpu->registers[reg_dst] = (word) (cpu->registers[reg_dst] + cpu->registers[reg_src]);
}

void init_math_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[add16_reg_reg] = add16_reg_reg_impl;
}
