#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(psh_reg16) {
    byte reg = cpu_read_inst_byte();
    stack_push(cpu, cpu_reg16(reg));
}

OPCODE_IMPL(psh_imm16) {
    word imm = cpu_read_inst_word();
    stack_push(cpu, imm);
}

OPCODE_IMPL(pop_reg16) {
    byte reg = cpu_read_inst_byte();
    cpu_reg16(reg) = stack_pop(cpu);
}

OPCODE_IMPL(call_mem16) {
    word adr = cpu_read_inst_word();
    stack_push_frame(cpu);
    cpu_reg16(ip) = adr;
}

OPCODE_IMPL(call_ptr) {
    byte reg = cpu_read_inst_byte();
    word adr = mmu_read_word(cpu->mmap, cpu_reg16(reg));
    stack_push_frame(cpu);
    cpu_reg16(ip) = adr;
}

OPCODE_IMPL(ret) {
    cpu_reg16(ip) = stack_pop_frame(cpu);
}

void init_stack_opcodes(struct cpu_t *cpu) {
    ASSIGN_OPCODE(psh_reg16);
    ASSIGN_OPCODE(psh_imm16);
    ASSIGN_OPCODE(pop_reg16);
    ASSIGN_OPCODE(call_mem16);
    ASSIGN_OPCODE(call_ptr);
    ASSIGN_OPCODE(ret);
}
