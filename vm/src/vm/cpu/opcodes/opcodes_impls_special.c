#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(halt) {
    cpu->flags.stop = 1;
}

void init_special_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[halt] = halt_impl;
}
