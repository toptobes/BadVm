#ifndef VM_CPU_H
#define VM_CPU_H

#include <stdio.h>
#include "../memory/mmu.h"

#define NUM_REGISTERS 11
#define STACK_START 0xff

struct cpu_t;

#include "opcodes/opcodes.h"
#include "../memory/mem.h"

typedef struct {
    unsigned stop : 1;
    unsigned zf   : 1;
    unsigned sf   : 1;
} flags_t;

typedef struct cpu_t {
    mmu_t *mmap;
    word registers[NUM_REGISTERS];
    opcode_impl opcodes[256];
    flags_t flags;
} cpu_t;

typedef enum {
    ip,
    sp, bp,
    ax, bx,
    cx, dx,
    ex, fx,
    gx, hx,
} reg16_t;

typedef enum {
    ah, al,
    bh, bl,
    ch, cl,
    dh, dl,
} reg8_t;

// -- GENERAL CPU STUFF --
cpu_t *cpu_new(mmu_t *mmap);

void cpu_step(cpu_t *cpu);
word cpu_run(cpu_t *cpu);

// -- STACK STUFF --
void stack_init(cpu_t *cpu, word start);

void stack_push(cpu_t *cpu, word word);
word stack_pop(cpu_t *cpu);

void stack_push_frame(cpu_t *cpu);
word stack_pop_frame(cpu_t *cpu);

// -- UTILS --
#define cpu_read_byte() (mmu_read_byte(cpu->mmap, cpu->registers[ip]++))
#define cpu_read_word() (cpu->registers[ip] += 2, mmu_read_word(cpu->mmap, cpu->registers[ip] - 2))

#define high_byte(reg) (reg * 2 + 1)
#define low_byte(reg) (reg * 2)

#define cpu_reg16(reg) cpu->registers[reg]
#define cpu_reg8h(reg) ((byte*) cpu->registers)[reg * 2 + 1]
#define cpu_reg8l(reg) ((byte*) cpu->registers)[reg * 2]
#define cpu_reg8(reg)  ((byte*) cpu->registers)[reg]

#endif //VM_CPU_H
