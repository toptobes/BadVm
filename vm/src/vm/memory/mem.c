#include <malloc.h>
#include "mem.h"
#include "stdio.h"

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

byte mem_read_byte(mem_t *m, int pos) {
    return m->mem[pos];
}

word mem_read_word(mem_t *m, int pos) {
    byte high = mem_read_byte(m, pos);
    byte low  = mem_read_byte(m, pos + 1);
    return (high << 8) | low; // NOLINT(cppcoreguidelines-narrowing-conversions)
}

void mem_write_byte(mem_t *m, int pos, byte b) {
    m->mem[pos] = b;
}

void mem_write_word(mem_t *m, int pos, word w) {
    mem_write_byte(m, pos, w >> 8); // NOLINT(cppcoreguidelines-narrowing-conversions)
    mem_write_byte(m, pos + 1, (byte) w);
}
