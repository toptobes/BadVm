#include "cpu.h"

void stack_init(cpu_t *cpu, word start) {
    cpu->registers[sp] = (word) start;
    cpu->registers[bp] = (word) start;
}

void stack_push(cpu_t *cpu, word word) {
    mem_write_word(cpu->mem, cpu->registers[sp], word);
    cpu->registers[sp] -= 2;
}

word stack_pop(cpu_t *cpu) {
    cpu->registers[sp] += 2;
    return mem_read_word(cpu->mem, cpu->registers[sp]);
}

void stack_push_frame(cpu_t *cpu) {
    word old_bp_adr = cpu->registers[bp];
    word new_bp_adr = cpu->registers[sp];

    stack_push(cpu, old_bp_adr);
    cpu->registers[bp] = new_bp_adr;

    word return_address = cpu->registers[ip] + 1;
    stack_push(cpu, return_address);
}

word stack_pop_frame(cpu_t *cpu) {
    cpu->registers[bp] = stack_pop(cpu);
    word ip_return_adr = stack_pop(cpu);
    return ip_return_adr;
}
