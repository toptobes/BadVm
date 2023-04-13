#include <malloc.h>
#include "cpu.h"

cpu_t *cpu_new(mem_t *mem){
    cpu_t *cpu = malloc(sizeof *cpu);
    cpu->mem = mem;

    flags_t flags = {0 };
    cpu->flags = flags;

    for (int i = 0; i < NUM_REGISTERS; i++) {
        cpu->registers[i] = 0;
    }

    opcodes_init(cpu);
    stack_init(cpu, 0xff);
    return cpu;
}

void cpu_dump_registers(cpu_t *cpu) {
    printf("ip: 0x%04x ", cpu->registers[ip]);
    printf("sp: 0x%04x ", cpu->registers[sp]);
    printf("bp: 0x%04x ", cpu->registers[bp]);
    printf("ax: 0x%04x ", cpu->registers[ax]);
    printf("bx: 0x%04x ", cpu->registers[bx]);
    printf("cx: 0x%04x ", cpu->registers[cx]);
    printf("dx: 0x%04x ", cpu->registers[dx]);
    puts("");
}

void cpu_dump_stack(cpu_t *cpu) {
    printf("stack:\n");
    for (int i = 0; i < 12; i += 2) {
        printf("  #%04x: 0x%04x\n", 0xff - i, mem_read_word(cpu->mem, 0xff - i));
    }
}

#define CPU_DUMP_ALL

void cpu_step(cpu_t *cpu) {
    byte opcode = cpu_read_byte();
    cpu->opcodes[opcode](cpu);

    #if defined(CPU_DUMP_OPCODE)    || defined(CPU_DUMP_ALL)
        printf("opcode: %x\n", opcode);
    #endif

    #if defined(CPU_DUMP_REGISTERS) || defined(CPU_DUMP_ALL)
        cpu_dump_registers(cpu);
    #endif

    #if defined(CPU_DUMP_STACK)     || defined(CPU_DUMP_ALL)
        cpu_dump_stack(cpu);
    #endif

    #if  defined(CPU_DUMP_REGISTERS) || defined(CPU_DUMP_STACK) || defined(CPU_DUMP_OPCODE) || defined(CPU_DUMP_ALL)
        printf("====================\n");
    #endif
}

void cpu_run(cpu_t *cpu) {
    while (!cpu->flags.stop) {
        cpu_step(cpu);
    }
}
