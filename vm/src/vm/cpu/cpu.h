#ifndef VM_CPU_H
#define VM_CPU_H

#include <stdio.h>
#include "../memory/mem.h"

#define NUM_REGISTERS 7
#define STACK_START 0xff

struct cpu_t;

#include "opcodes/opcodes.h"

typedef struct {
    unsigned stop : 1;
    unsigned zf   : 1;
} flags_t;

typedef struct cpu_t {
    mem_t *mem;
    word registers[NUM_REGISTERS];
    opcode_impl opcodes[256];
    flags_t flags;
} cpu_t;

typedef enum {
    ip,
    sp, bp,
    ax, bx,
    cx, dx,
} reg_t;

// -- GENERAL CPU STUFF --
cpu_t *cpu_new(mem_t *mem);

void cpu_step(cpu_t *cpu);
word cpu_run(cpu_t *cpu);

// -- STACK STUFF --
void stack_init(cpu_t *cpu, word start);

void stack_push(cpu_t *cpu, word word);
word stack_pop(cpu_t *cpu);

void stack_push_frame(cpu_t *cpu);
word stack_pop_frame(cpu_t *cpu);

// -- UTILS --
#define cpu_read_byte() (mem_read_byte(cpu->mem, cpu->registers[ip]++))
#define cpu_read_word() (cpu->registers[ip] += 2, mem_read_word(cpu->mem, cpu->registers[ip] - 2))

#define cpu_write_byte(b) (mem_write_byte(cpu->mem, cpu->registers[ip]++, b))
#define cpu_write_word(w) (cpu->registers[ip] += 2, mem_write_word(cpu->mem, cpu->registers[ip] - 2, w))

#endif //VM_CPU_H
