#ifndef VM_DEBUG_H
#define VM_DEBUG_H

//#define CPU_DEBUG_DUMP_ALL
//#define CPU_DEBUG_TIME_LOOPS
//#define CPU_DEBUG_STEP_MANUALLY

int stack_count = 0;

#define CPU_DUMP_REGISTERS(cpu) do {        \
    printf("ip: 0x%04x ", cpu_reg16(ip));   \
    printf("sp: 0x%04x ", cpu_reg16(sp));   \
    printf("bp: 0x%04x ", cpu_reg16(bp));   \
    printf("ax: 0x%04x ", cpu_reg16(ax));   \
    printf("bx: 0x%04x ", cpu_reg16(bx));   \
    printf("cx: 0x%04x ", cpu_reg16(cx));   \
    printf("dx: 0x%04x ", cpu_reg16(dx));   \
    puts("");                               \
} while(0)

#define CPU_DUMP_STACK(cpu) do {                                                                      \
    printf("stack:\n");                                                                               \
    for (int i = -4; i < 8; i += 2) {                                                                 \
        if (cpu_reg16(sp) < i || STACK_START < cpu_reg16(sp) - i)                                 \
            continue;                                                                                 \
                                                                                                      \
        printf("  #%04x: 0x%04x", cpu_reg16(sp) - i, mmap_read_word(cpu->mmap, cpu_reg16(sp) - i));   \
                                                                                                      \
        if (i == 0 && cpu_reg16(sp) == cpu_reg16(bp))                                                 \
            printf(" <-- sp, bp");                                                                    \
        else if (i == 0)                                                                              \
            printf(" <-- sp");                                                                        \
        else if (cpu_reg16(bp) + i == cpu_reg16(sp))                                                  \
            printf(" <-- bp");                                                                        \
                                                                                                      \
        puts("");                                                                                     \
    }                                                                                                 \
} while(0)

#define CPU_DUMP_OPCODE(opcode) do { \
    printf("opcode: %d\n", opcode);  \
} while(0)

#endif //VM_DEBUG_H
