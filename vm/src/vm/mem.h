#ifndef VM_MEM_H
#define VM_MEM_H

#include "types.h"

typedef struct {
    byte  *mem;
    size_t size;
    int    pos;
} mem_t;

mem_t* mem_new(size_t size);
void mem_destroy(mem_t **m);

#define mem_read_byte(m) mem_read_byte_at_pos(m, m->pos++)
#define mem_read_word(m) mem_read_word_at_pos(m, m->pos++)

byte mem_read_byte_at_pos(mem_t *m, int pos);
word mem_read_word_at_pos(mem_t *m, int pos);

#define mem_write_byte(m, b) mem_write_byte_at_pos(m, m->pos++, b)
#define mem_write_word(m, w) mem_write_word_at_pos(m, m->pos++, w)

void mem_write_byte_at_pos(mem_t *m, int pos, byte b);
void mem_write_word_at_pos(mem_t *m, int pos, word w);

#endif //VM_MEM_H
