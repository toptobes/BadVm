#include <malloc.h>
#include <conio.h>
#include <time.h>
#include "cpu.h"
#include "../debug.h"

static void mem_init(cpu_t *cpu, mem_t *mem);

cpu_t *cpu_new(mem_t *mem){
    cpu_t *cpu = malloc(sizeof *cpu);

    flags_t flags = { 0 };
    cpu->flags = flags;

    for (int i = 0; i < NUM_REGISTERS; i++) {
        cpu_reg16(i) = 0;
    }

    mem_init(cpu, mem);
    opcodes_init(cpu);
    stack_init(cpu, STACK_START);
    return cpu;
}

static void mem_init(cpu_t *cpu ,mem_t *mem) {
    mm_device_t *device = malloc(sizeof *device);
    device->raw_device = mem;
    device->read_byte  = (byte (*)(void *, int)) mem_read_byte;
    device->read_word  = (word (*)(void *, int)) mem_read_word;
    device->write_byte = (void (*)(void *, int, byte)) mem_write_byte;
    device->write_word = (void (*)(void *, int, word)) mem_write_word;

    mm_mapping_t *mapping = mm_mapping_new(device, 0, mem->size);
    mem_map_t *map = mem_map_new();
    mmap_add(map, mapping);

    cpu->mmap = map;
}

int stack_count = 0;

void cpu_step(cpu_t *cpu) {
    #ifdef CPU_DEBUG_TIME_LOOPS
        clock_t begin = clock();
    #endif

    byte opcode = cpu_read_byte();
    cpu->opcodes[opcode](cpu);

    #ifdef CPU_DEBUG_DUMP_ALL
        printf("\n=========%03d=========\n", ++stack_count);

        #ifdef CPU_DEBUG_TIME_LOOPS
            clock_t end = clock();
            printf("time: %.3f ms\n", (double)(end - begin) / CLOCKS_PER_SEC);
        #endif

        CPU_DUMP_OPCODE(opcode);
        CPU_DUMP_REGISTERS(cpu);
        CPU_DUMP_STACK(cpu);
        printf("=====================\n");
    #endif
}

word cpu_run(cpu_t *cpu) {
    cpu_reg16(ip) = cpu_read_word();

    #ifdef CPU_DEBUG_DUMP_ALL
        printf("\n========START========\n");
        CPU_DUMP_REGISTERS(cpu);
        CPU_DUMP_STACK(cpu);
        printf("=====================\n");
    #endif

    while (!cpu->flags.stop) {
        #ifdef CPU_DEBUG_STEP_MANUALLY
            getch();
        #endif

        cpu_step(cpu);
    }

    #ifdef CPU_DEBUG_DUMP_ALL
        puts("");
    #endif

    return cpu_reg16(ax);
}
