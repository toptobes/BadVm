#ifndef VM_VM_H
#define VM_VM_H

#include <malloc.h>
#include "cpu/cpu.h"

typedef struct {
    cpu_t *cpu;
    mmu_t *mmu;
} vm_t;

void vm_init(vm_t *vm, word mem_size);

int vm_run(vm_t *vm);

void vm_inject_code(vm_t *vm, byte *code, size_t size);

void vm_add_mem_mapping(vm_t *vm, mm_device_t *device, int start, int end);

#endif //VM_VM_H
