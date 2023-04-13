#ifndef VM_CPU_H
#define VM_CPU_H

#include <stdio.h>
#include "../memory/mem.h"

#define NUM_REGISTERS 7

struct cpu_t;
typedef void (*opcode_impl)(struct cpu_t* cpu);

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

typedef enum {
    halt = 0x0,

    mov_reg_reg = 0x10,
    mov_imm_reg,
    mov_reg_mem,
    mov_mem_reg,

    add_reg_reg = 0x20,

    jmp_not_eql = 0x30,

    stk_psh_reg = 0x40,
    stk_psh_imm,
    stk_pop_reg,
    call_reg,
    call_imm,
    ret,

    cmp = 0x50,
} opcode_t;

// -- GENERAL CPU STUFF --
cpu_t *cpu_new(mem_t *mem);

void cpu_step(cpu_t *cpu);
void cpu_run(cpu_t *cpu);

void cpu_dump_registers(cpu_t *cpu);
void cpu_dump_stack(cpu_t *cpu);

// -- OPCODE STUFF --
void opcodes_init(cpu_t *cpu);

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
