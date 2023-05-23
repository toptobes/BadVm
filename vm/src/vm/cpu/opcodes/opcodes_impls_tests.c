#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(cmp_reg16_reg16) {
    byte reg1 = cpu_read_inst_byte();
    byte reg2 = cpu_read_inst_byte();
    word result = cpu_reg16(reg1) - cpu_reg16(reg2);

    cpu->flags.zf = (result == 0);
    cpu->flags.sf = (result >> 15);
}

OPCODE_IMPL(cmp_reg16_imm16) {
    byte reg = cpu_read_inst_byte();
    byte imm = cpu_read_inst_word();
    word result = cpu_reg16(reg) - imm;

    cpu->flags.zf = (result == 0);
    cpu->flags.sf = (result >> 15);
}

void init_test_opcodes(struct cpu_t *cpu) {
    ASSIGN_OPCODE(cmp_reg16_reg16);
    ASSIGN_OPCODE(cmp_reg16_imm16);
}
