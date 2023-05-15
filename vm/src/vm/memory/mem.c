#include <malloc.h>
#include "mem.h"

mem_t *mem_new(size_t size) {
    mem_t *mem = malloc(sizeof *mem);
    mem->size = size;
    mem->mem  = calloc(size, 1);
    return mem;
}

void mem_destroy(mem_t **mem) {
    free((*mem)->mem);
    free(*mem);
    mem = NULL;
}

byte mem_read_byte(mem_t *m, int addr) {
    return m->mem[addr];
}

word mem_read_word(mem_t *m, int addr) {
    byte high = mem_read_byte(m, addr);
    byte low  = mem_read_byte(m, addr + 1);
    return (high << 8) | low; // NOLINT(cppcoreguidelines-narrowing-conversions)
}

void mem_write_byte(mem_t *m, int addr, byte b) {
    m->mem[addr] = b;
}

void mem_write_word(mem_t *m, int addr, word w) {
    mem_write_byte(m, addr, w >> 8); // NOLINT(cppcoreguidelines-narrowing-conversions)
    mem_write_byte(m, addr + 1, (byte) w);
}
