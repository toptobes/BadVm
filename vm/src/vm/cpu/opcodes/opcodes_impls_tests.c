#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(cmp) {
    byte reg1 = cpu_read_byte();
    byte reg2 = cpu_read_byte();
    word result = cpu->registers[reg1] - cpu->registers[reg2];
    cpu->flags.zf = (result == 0);
}

void init_test_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[cmp] = cmp_impl;
}
