#ifndef VM_MEMMAP_H
#define VM_MEMMAP_H

#include <stdbool.h>
#include "../types.h"

#define MAX_DEVICES 64

typedef byte (*byte_reader)(void* device, int pos);
typedef word (*word_reader)(void* device, int pos);
typedef void (*byte_writer)(void* device, int pos, byte b);
typedef void (*word_writer)(void* device, int pos, word w);

typedef struct {
    void *raw_device;
    byte_reader byte_reader;
    word_reader word_reader;
    byte_writer byte_writer;
    word_writer word_writer;
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

#define mm_device_new(...) f_mm_device_new((struct mm_device_new_args) { __VA_ARGS__ })
struct mm_device_new_args { void* raw_device; byte_reader br; word_reader wr; byte_writer bw; word_writer ww; };
mm_device_t  *f_mm_device_new(struct mm_device_new_args);

void mmap_add(mem_map_t*, mm_mapping_t*);
void mmap_rem(mem_map_t*, mm_mapping_t*);

byte mmap_read_byte(mem_map_t*, int pos);
word mmap_read_word(mem_map_t*, int pos);
void mmap_write_byte(mem_map_t*, int pos, byte b);
void mmap_write_word(mem_map_t*, int pos, word w);

#endif //VM_MEMMAP_H
