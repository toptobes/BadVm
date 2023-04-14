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

#define cpu_write_byte(b) (mmap_write_byte(cpu->mmap, i++, b))
#define cpu_write_word(w) (i += 2, mmap_write_word(cpu->mmap, i - 2, w))

    int i = 0;
    cpu_write_byte(mov_imm16_reg16);
    cpu_write_byte(ax);
    cpu_write_word(3);

    cpu_write_byte(mov_imm16_reg16);
    cpu_write_byte(bx);
    cpu_write_word(2);

    cpu_write_byte(cmp_reg16_reg16);
    cpu_write_byte(ax);
    cpu_write_byte(bx);

    cpu_write_byte(jlt_imm);
    cpu_write_word(23);

    cpu_write_byte(mov_imm16_reg16);
    cpu_write_byte(cx);
    cpu_write_word('n');

    cpu_write_byte(mov_reg16_mem);
    cpu_write_word(400);
    cpu_write_byte(cx);

    cpu_write_byte(halt);

    cpu_write_byte(mov_imm16_reg16);
    cpu_write_byte(cx);
    cpu_write_word('y');

    cpu_write_byte(mov_reg16_mem);
    cpu_write_word(400);
    cpu_write_byte(cx);

    cpu_run(cpu);
}
