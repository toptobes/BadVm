#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(stk_psh16_reg) {
    byte reg = cpu_read_byte();
    stack_push(cpu, cpu_reg16(reg));
}

OPCODE_IMPL(stk_psh16_imm) {
    word imm = cpu_read_word();
    stack_push(cpu, imm);
}

OPCODE_IMPL(stk_pop16_reg) {
    byte reg = cpu_read_byte();
    cpu_reg16(reg) = stack_pop(cpu);
}

OPCODE_IMPL(call_imm) {
    word adr = cpu_read_word();
    stack_push_frame(cpu);
    cpu_reg16(ip) = adr;
}

OPCODE_IMPL(call_reg) {
    byte reg = cpu_read_byte();
    word adr = cpu_reg16(reg);
    stack_push_frame(cpu);
    cpu_reg16(ip) = adr;
}

OPCODE_IMPL(ret) {
    cpu_reg16(ip) = stack_pop_frame(cpu);
}

void init_stack_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[stk_psh16_reg] = stk_psh16_reg_impl;
    cpu->opcodes[stk_psh16_imm] = stk_psh16_imm_impl;
    cpu->opcodes[stk_pop16_reg] = stk_pop16_reg_impl;
    cpu->opcodes[call_imm   ] = call_imm_impl;
    cpu->opcodes[call_reg   ] = call_reg_impl;
    cpu->opcodes[ret        ] = ret_impl;
}
