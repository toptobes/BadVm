#include "cpu.h"

void stack_init(cpu_t *cpu, word start) {
    cpu_reg16(sp) = start;
    cpu_reg16(bp) = start;
}

void stack_push(cpu_t *cpu, word word) {
    mmu_write_word(cpu->mmap, cpu_reg16(sp), word);
    cpu_reg16(sp) -= 2;
}

word stack_pop(cpu_t *cpu) {
    cpu_reg16(sp) += 2;
    return mmu_read_word(cpu->mmap, cpu_reg16(sp));
}

void stack_push_frame(cpu_t *cpu) {
    word old_bp_adr = cpu_reg16(bp);
    word new_bp_adr = cpu_reg16(sp);

    stack_push(cpu, old_bp_adr);
    cpu_reg16(bp) = new_bp_adr;

    word return_address = cpu_reg16(ip);
    stack_push(cpu, return_address);
}

word stack_pop_frame(cpu_t *cpu) {
    word ip_return_adr = stack_pop(cpu);
    cpu_reg16(bp) = stack_pop(cpu);
    return ip_return_adr;
}
