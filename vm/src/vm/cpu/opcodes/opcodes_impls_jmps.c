#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(jmp_not_eql) {
    word adr = cpu_read_word();

    if (!cpu->flags.zf) {
        cpu->registers[ip] = adr;
    }
}

void init_jump_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[jmp_not_eql] = jmp_not_eql_impl;
}
