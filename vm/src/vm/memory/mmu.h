#ifndef VM_MMU_H
#define VM_MMU_H

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
} mmu_t;

mmu_t *mmu_new();
mm_mapping_t *mm_mapping_new(mm_device_t*, size_t start, size_t end);

#define mm_device_new(...) f_mm_device_new((struct mm_device_new_args) { __VA_ARGS__ })
struct mm_device_new_args { void* raw_device; byte_reader br; word_reader wr; byte_writer bw; word_writer ww; };
mm_device_t *f_mm_device_new(struct mm_device_new_args);

void mmu_add(mmu_t*, mm_mapping_t*);
void mmu_rem(mmu_t*, mm_mapping_t*);

byte mmu_read_byte(mmu_t*, int pos);
word mmu_read_word(mmu_t*, int pos);
void mmu_write_byte(mmu_t*, int pos, byte b);
void mmu_write_word(mmu_t*, int pos, word w);

void *mmu_get_root_device(mmu_t*);

#endif //VM_MMU_H
