#include <malloc.h>
#include <conio.h>
#include "cpu.h"
#include "../debug.h"

cpu_t *cpu_new(mem_t *mem){
    cpu_t *cpu = malloc(sizeof *cpu);
    cpu->mem = mem;

    flags_t flags = { 0 };
    cpu->flags = flags;

    for (int i = 0; i < NUM_REGISTERS; i++) {
        cpu->registers[i] = 0;
    }

    opcodes_init(cpu);
    stack_init(cpu, STACK_START);
    return cpu;
}

#define CPU_DEBUG (CPU_DEBUG_STEP_MANUALLY | CPU_DEBUG_DUMP_ALL)

void cpu_step(cpu_t *cpu) {
    byte opcode = cpu_read_byte();
    cpu->opcodes[opcode](cpu);

    #if CPU_DEBUG & CPU_DEBUG_DUMP_ALL
        printf("\n=========%03d=========\n", ++stack_count);
        CPU_DUMP_OPCODE(opcode);
        CPU_DUMP_REGISTERS(cpu);
        CPU_DUMP_STACK(cpu);
        printf("=====================\n");
    #endif
}

word cpu_run(cpu_t *cpu) {
    #if CPU_DEBUG & CPU_DEBUG_DUMP_ALL
        printf("\n========START========\n");
        CPU_DUMP_REGISTERS(cpu);
        CPU_DUMP_STACK(cpu);
        printf("=====================\n");
    #endif

    while (!cpu->flags.stop) {
        #if CPU_DEBUG & CPU_DEBUG_STEP_MANUALLY
            getch();
        #endif

        cpu_step(cpu);
    }

    #if CPU_DEBUG & CPU_DEBUG_DUMP_ALL
        puts("");
    #endif

    return cpu->registers[ax];
}
