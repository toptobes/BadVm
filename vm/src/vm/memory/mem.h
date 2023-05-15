#ifndef VM_MEM_H
#define VM_MEM_H

#include "../types.h"

typedef struct {
    byte  *mem;
    size_t size;
} mem_t;

mem_t* mem_new(size_t size);
void mem_destroy(mem_t **m);

byte mem_read_byte(mem_t *m, int addr);
word mem_read_word(mem_t *m, int addr);

void mem_write_byte(mem_t *m, int addr, byte b);
void mem_write_word(mem_t *m, int addr, word w);

#endif //VM_MEM_H
