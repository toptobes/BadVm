#include <malloc.h>
#include "mem.h"

mem_t *mem_new(size_t size) {
    mem_t *mem = malloc(sizeof *mem);
    mem->size = size;
    mem->pos  = 0;
    mem->mem  = calloc(size, 1);
    return mem;
}

void mem_destroy(mem_t **mem) {
    free((*mem)->mem);
    free(*mem);
    mem = NULL;
}

byte mem_read_byte_at_pos(mem_t *mem, int pos) {
    return mem->mem[pos];
}

word mem_read_word_at_pos(mem_t *mem, int pos) {
    byte byte1 = mem_read_byte_at_pos(mem, pos);
    byte byte2 = mem_read_byte_at_pos(mem, pos);
    return (byte2 << 8) | byte1; // NOLINT(cppcoreguidelines-narrowing-conversions)
}

void mem_write_byte_at_pos(mem_t *mem, int pos, byte b) {
    mem->mem[pos] = b;
}

void mem_write_word_at_pos(mem_t *mem, int pos, word w) {
    mem_write_byte_at_pos(mem, pos, w >> 8); // NOLINT(cppcoreguidelines-narrowing-conversions)
    mem_write_byte_at_pos(mem, pos, (byte) w);
}
