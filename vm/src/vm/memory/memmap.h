#ifndef VM_MEMMAP_H
#define VM_MEMMAP_H

#include <stdbool.h>
#include "../types.h"

#define MAX_DEVICES 64

typedef struct {
    void *raw_device;
    byte (*read_byte)(void* device, int pos);
    word (*read_word)(void* device, int pos);
    void (*write_byte)(void* device, int pos, byte b);
    void (*write_word)(void* device, int pos, word w);
} mm_device_t;

typedef struct {
    mm_device_t *device;
    size_t start;
    size_t end;
    bool is_absolute;
} mm_mapping_t;

typedef struct {
    int num_devices;
    mm_mapping_t *mapping[MAX_DEVICES];
} mem_map_t;

mem_map_t    *mem_map_new();
mm_mapping_t *mm_mapping_new(mm_device_t*, size_t start, size_t end);

void mmap_add(mem_map_t*, mm_mapping_t*);
void mmap_rem(mem_map_t*, mm_mapping_t*);

byte mmap_read_byte(mem_map_t*, int pos);
word mmap_read_word(mem_map_t*, int pos);
void mmap_write_byte(mem_map_t*, int pos, byte b);
void mmap_write_word(mem_map_t*, int pos, word w);

#endif //VM_MEMMAP_H
