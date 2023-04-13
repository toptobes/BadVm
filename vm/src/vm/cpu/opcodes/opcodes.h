#ifndef VM_OPCODES_H
#define VM_OPCODES_H

#define OPCODE_IMPL(name) static void name##_impl(cpu_t *cpu)

typedef enum {
    halt = 0x0,

    mov16_reg_reg = 0x10,
    mov16_imm_reg,
    mov16_reg_mem,
    mov16_mem_reg,

    add16_reg_reg = 0x20,

    jmp_not_eql   = 0x30,

    stk_psh16_reg = 0x40,
    stk_psh16_imm,
    stk_pop16_reg,
    call_reg,
    call_imm,
    ret,

    cmp = 0x50,
} opcode_t;

struct cpu_t;
typedef void (*opcode_impl)(struct cpu_t* cpu);

void opcodes_init(struct cpu_t *cpu);

void init_mov_opcodes(struct cpu_t *cpu);
void init_math_opcodes(struct cpu_t *cpu);
void init_stack_opcodes(struct cpu_t *cpu);
void init_jump_opcodes(struct cpu_t *cpu);
void init_special_opcodes(struct cpu_t *cpu);
void init_test_opcodes(struct cpu_t *cpu);

#endif //VM_OPCODES_H
