#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(psh_reg16) {
    byte reg = cpu_read_byte();
    stack_push(cpu, cpu_reg16(reg));
}

OPCODE_IMPL(psh_imm16) {
    word imm = cpu_read_word();
    stack_push(cpu, imm);
}

OPCODE_IMPL(pop_reg16) {
    byte reg = cpu_read_byte();
    cpu_reg16(reg) = stack_pop(cpu);
}

OPCODE_IMPL(call_mem) {
    word adr = cpu_read_word();
    stack_push_frame(cpu);
    cpu_reg16(ip) = adr;
}

OPCODE_IMPL(call_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));
    stack_push_frame(cpu);
    cpu_reg16(ip) = adr;
}

OPCODE_IMPL(ret) {
    cpu_reg16(ip) = stack_pop_frame(cpu);
}

void init_stack_opcodes(struct cpu_t *cpu) {
    ASSIGN_OPCODE(psh_reg16, psh_reg16_impl);
    ASSIGN_OPCODE(psh_imm16, psh_imm16_impl);
    ASSIGN_OPCODE(pop_reg16, pop_reg16_impl);
    ASSIGN_OPCODE(call_mem, call_mem_impl);
    ASSIGN_OPCODE(call_ptr, call_ptr_impl);
    ASSIGN_OPCODE(ret, ret_impl);
}
