#include "cpu.h"

#define OPCODE_IMPL(name) static void name##_impl(cpu_t *cpu)

OPCODE_IMPL(mov_reg_reg) {
    byte reg_dst = cpu_read_byte();
    byte reg_src = cpu_read_byte();
    cpu->registers[reg_dst] = cpu->registers[reg_src];
}

OPCODE_IMPL(mov_imm_reg) {
    byte reg = cpu_read_byte();
    word imm = cpu_read_word();
    cpu->registers[reg] = imm;
}

OPCODE_IMPL(mov_mem_reg) {
    byte reg = cpu_read_byte();
    word adr = cpu_read_word();
    cpu->registers[reg] = mem_read_word(cpu->mem, adr);
}

OPCODE_IMPL(mov_reg_mem) {
    word adr = cpu_read_word();
    byte reg = cpu_read_byte();
    mem_write_word(cpu->mem, adr, cpu->registers[reg]);
}

OPCODE_IMPL(add_reg_reg) {
    byte reg_dst = cpu_read_byte();
    byte reg_src = cpu_read_byte();
    cpu->registers[reg_dst] = (word) (cpu->registers[reg_dst] + cpu->registers[reg_src]);
}

OPCODE_IMPL(stk_psh_reg) {
    byte reg = cpu_read_byte();
    stack_push(cpu, cpu->registers[reg]);
}

OPCODE_IMPL(stk_psh_imm) {
    word imm = cpu_read_word();
    stack_push(cpu, imm);
}

OPCODE_IMPL(stk_pop_reg) {
    byte reg = cpu_read_byte();
    cpu->registers[reg] = stack_pop(cpu);
}

OPCODE_IMPL(call_imm) {
    word adr = cpu_read_word();
    stack_push_frame(cpu);
    cpu->registers[ip] = adr;
}

OPCODE_IMPL(call_reg) {
    byte reg = cpu_read_byte();
    word adr = cpu->registers[reg];
    stack_push_frame(cpu);
    cpu->registers[ip] = adr;
}

OPCODE_IMPL(ret) {
    cpu->registers[ip] = stack_pop_frame(cpu);
}

OPCODE_IMPL(cmp) {
    byte reg1 = cpu_read_byte();
    byte reg2 = cpu_read_byte();
    word result = cpu->registers[reg1] - cpu->registers[reg2];
    cpu->flags.zf = (result == 0);
}

OPCODE_IMPL(jmp_not_eql) {
    word adr = cpu_read_word();

    if (!cpu->flags.zf) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(halt) {
    cpu->flags.stop = 1;
}

void opcodes_init(cpu_t *cpu) {
    cpu->opcodes[mov_reg_reg] = mov_reg_reg_impl;
    cpu->opcodes[mov_imm_reg] = mov_imm_reg_impl;
    cpu->opcodes[mov_mem_reg] = mov_mem_reg_impl;
    cpu->opcodes[mov_reg_mem] = mov_reg_mem_impl;
    cpu->opcodes[add_reg_reg] = add_reg_reg_impl;
    cpu->opcodes[stk_psh_reg] = stk_psh_reg_impl;
    cpu->opcodes[stk_psh_imm] = stk_psh_imm_impl;
    cpu->opcodes[stk_pop_reg] = stk_pop_reg_impl;
    cpu->opcodes[jmp_not_eql] = jmp_not_eql_impl;
    cpu->opcodes[call_imm   ] = call_imm_impl;
    cpu->opcodes[call_reg   ] = call_reg_impl;
    cpu->opcodes[halt       ] = halt_impl;
    cpu->opcodes[ret        ] = ret_impl;
    cpu->opcodes[cmp        ] = cmp_impl;
}
