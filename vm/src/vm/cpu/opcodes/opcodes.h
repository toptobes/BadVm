#ifndef VM_OPCODES_H
#define VM_OPCODES_H

#define OPCODE_IMPL(name) static void name##_impl(cpu_t *cpu)

typedef enum {
    halt = 0,

    mov_reg16_reg16 = 10,
    mov_imm16_reg16,
    mov_mem_reg16,
    mov_reg16_mem,
    mov_ptr_reg16,
    mov_reg8_reg8,
    mov_imm8_reg8,
    mov_mem_reg8,
    mov_reg8_mem,

    add_reg16_reg16 = 20,
    add_imm16_reg16,
    sub_reg16_reg16,
    sub_imm16_reg16,
    mul_reg16_reg16,
    mul_imm16_reg16,

    add_reg8_reg8,
    add_imm8_reg8,
    sub_reg8_reg8,
    sub_imm8_reg8,
    mul_reg8_reg8,
    mul_imm8_reg8,

    shr_reg16_imm8 = 40,
    shr_reg16_reg8,
    shl_reg16_imm8,
    shl_reg16_reg8,
    sar_reg16_imm8,
    sar_reg16_reg8,
    and_reg16_reg16,
    and_reg16_imm16,
    or_reg16_reg16,
    or_reg16_imm16,
    xor_reg16_reg16,
    xor_reg16_imm16,
    not_reg16,

    jne_imm = 60,
    jeq_imm,
    jgt_imm,
    jlt_imm,
    jge_imm,
    jle_imm,
    jne_reg,
    jeq_reg,
    jgt_reg,
    jlt_reg,
    jge_reg,
    jle_reg,

    stk_psh16_reg = 80,
    stk_psh16_imm,
    stk_pop16_reg,
    call_reg,
    call_imm,
    ret,

    cmp_reg16_reg16 = 90,
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
