#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(cmp_reg16_reg16) {
    byte reg1 = cpu_read_byte();
    byte reg2 = cpu_read_byte();
    word result = cpu_reg16(reg1) - cpu_reg16(reg2);
    cpu->flags.zf = (result == 0);
    cpu->flags.sf = (result >> 15);

    printf("%d\n", cpu->flags.zf);
    printf("%d\n", cpu->flags.sf);
}

void init_test_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[cmp_reg16_reg16] = cmp_reg16_reg16_impl;
}
