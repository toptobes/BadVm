#ifndef VM_OPCODES_H
#define VM_OPCODES_H

#include "../../debug.h"

#define OPCODE_IMPL(name) static void name##_impl(cpu_t *cpu)

#ifdef CPU_DEBUG_DUMP_ALL
    extern char* opcode_name_lookup[256];

    #define ASSIGN_OPCODE(name) do {  \
        cpu->opcodes[name] = name##_impl;       \
        opcode_name_lookup[name] = #name;       \
    } while (0)
#else
    #define ASSIGN_OPCODE(name) \
        (cpu->opcodes[name] = name##_impl)
#endif

typedef enum {
    #include "../../../../../opcodes"
} opcode_t;

struct cpu_t;
typedef void (*opcode_impl)(struct cpu_t* cpu);

void opcodes_init(struct cpu_t *cpu);

void init_mov_opcodes(struct cpu_t *cpu);
void init_math_opcodes(struct cpu_t *cpu);
void init_logic_opcodes(struct cpu_t *cpu);
void init_stack_opcodes(struct cpu_t *cpu);
void init_jump_opcodes(struct cpu_t *cpu);
void init_special_opcodes(struct cpu_t *cpu);
void init_test_opcodes(struct cpu_t *cpu);

#endif //VM_OPCODES_H
