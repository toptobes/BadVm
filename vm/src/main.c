#include <malloc.h>
#include "vm/cpu/cpu.h"
#include "vm/memory/mem.h"
#include "vm/memory/memmap.h"

static byte char_io_read_byte(void* device, int pos) {
    return 0;
}

static word char_io_read_word(void* device, int pos) {
    return 0;
}

static void char_io_write_byte(void* device, int pos, byte byte) {
    printf("%c", byte);
}

static void char_io_write_word(void* device, int pos, word word) {
    printf("%c", word);
}

int main() {
    mem_t *mem = mem_new(1024);
    cpu_t *cpu = cpu_new(mem);

    mm_device_t *char_io = malloc(sizeof *char_io);
    char_io->raw_device = NULL;
    char_io->read_byte = char_io_read_byte;
    char_io->read_word = char_io_read_word;
    char_io->write_byte = char_io_write_byte;
    char_io->write_word = char_io_write_word;

    mm_mapping_t *mapping = mm_mapping_new(char_io, 400, 400);

    mmap_add(cpu->mmap, mapping);

    byte instructions[] = {
        #include "../../out"
    };
    for (int i = 0; i < (sizeof instructions / sizeof *instructions); i++) {
        mmap_write_byte(cpu->mmap, i, instructions[i]);
    }

    return cpu_run(cpu);
}
