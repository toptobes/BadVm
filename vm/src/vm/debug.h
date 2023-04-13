#ifndef VM_DEBUG_H
#define VM_DEBUG_H

#define CPU_DEBUG_DUMP_ALL       0b01
#define CPU_DEBUG_STEP_MANUALLY  0b10

int stack_count = 0;

#define CPU_DUMP_REGISTERS(cpu) do {           \
    printf("ip: 0x%04x ", cpu->registers[ip]); \
    printf("sp: 0x%04x ", cpu->registers[sp]); \
    printf("bp: 0x%04x ", cpu->registers[bp]); \
    printf("ax: 0x%04x ", cpu->registers[ax]); \
    printf("bx: 0x%04x ", cpu->registers[bx]); \
    printf("cx: 0x%04x ", cpu->registers[cx]); \
    printf("dx: 0x%04x ", cpu->registers[dx]); \
    puts("");                                  \
} while(0)

#define CPU_DUMP_STACK(cpu) do {                                                                            \
    printf("stack:\n");                                                                                     \
    for (int i = -4; i < 8; i += 2) {                                                                       \
        if (cpu->registers[sp] - i < 0 || STACK_START < cpu->registers[sp] - i)                             \
            continue;                                                                                       \
                                                                                                            \
        printf("  #%04x: 0x%04x", cpu->registers[sp] - i, mem_read_word(cpu->mem, cpu->registers[sp] - i)); \
                                                                                                            \
        if (i == 0 && cpu->registers[sp] == cpu->registers[bp])                                             \
            printf(" <-- sp, bp");                                                                          \
        else if (i == 0)                                                                                    \
            printf(" <-- sp");                                                                              \
        else if (cpu->registers[sp] - i == cpu->registers[bp])                                              \
            printf(" <-- bp");                                                                              \
                                                                                                            \
        puts("");                                                                                           \
    }                                                                                                       \
} while(0)

#define CPU_DUMP_OPCODE(opcode) do { \
    printf("opcode: %x\n", opcode);  \
} while(0)

#endif //VM_DEBUG_H
