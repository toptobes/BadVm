#ifndef VM_CPU_H
#define VM_CPU_H

#include "mem.h"

#define NUM_REGISTERS 6

typedef struct {
    mem_t *mem;
    word registers[NUM_REGISTERS];
} cpu_t;

typedef enum {
    ip, acc, ax, bx, cx, dx
} reg_t;

cpu_t *cpu_new(mem_t *mem);

#endif //VM_CPU_H
