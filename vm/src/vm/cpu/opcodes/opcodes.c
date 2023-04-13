#include "opcodes.h"

void opcodes_init(struct cpu_t *cpu) {
    init_mov_opcodes(cpu);
    init_math_opcodes(cpu);
    init_stack_opcodes(cpu);
    init_jump_opcodes(cpu);
    init_special_opcodes(cpu);
    init_test_opcodes(cpu);
}
