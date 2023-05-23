#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(halt) {
    cpu->flags.stop = 1;
}

OPCODE_IMPL(nop) {}

void init_special_opcodes(struct cpu_t *cpu) {
    ASSIGN_OPCODE(halt);
    ASSIGN_OPCODE(nop);
}
