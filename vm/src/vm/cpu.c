#include <malloc.h>
#include "cpu.h"

cpu_t *cpu_new(mem_t *mem) {
    cpu_t *cpu = malloc(sizeof *cpu);
    cpu->mem = mem;
    return cpu;
}
