#ifndef VM_MEMMAP_H
#define VM_MEMMAP_H

#include <stdbool.h>
#include "../types.h"

typedef struct {
    void *device;
    byte (*read_byte)(void* device);
    word (*read_word)(void* device);
    void (*write_byte)(void* device, byte, int pos);
    void (*write_word)(void* device, word, int pos);
} mm_device;

typedef struct {
    mm_device *device;
    size_t start;
    size_t end;
    bool is_absolute;
} mm_mapping;

typedef struct {
    mm_mapping mapping[64];
} mem_map_t;

mm_mapping *mm_mapping_new(mm_device*, size_t start, size_t end);

void mm_map_add(mem_map_t*, mm_mapping*);
void mm_map_rem(mem_map_t*, mm_mapping*);

#endif //VM_MEMMAP_H
