#include <malloc.h>
#include <string.h>
#include "mmu.h"

mmu_t* mmu_new() {
    mmu_t *map = malloc(sizeof *map);
    map->num_devices = 0;
    return map;
}

mm_mapping_t* mm_mapping_new(mm_device_t *device, size_t start, size_t end) {
    mm_mapping_t *mapping = malloc(sizeof *mapping);
    mapping->device = device;
    mapping->start  = start;
    mapping->end    = end;
    mapping->is_absolute = false;
    return mapping;
}

static byte null_byte_reader(void* device, int pos) { return 0; }
static word null_word_reader(void* device, int pos) { return 0; }
static void null_byte_writer(void* device, int pos, byte byte) {}
static void null_word_writer(void* device, int pos, word word) {}

mm_device_t *f_mm_device_new(struct mm_device_new_args args) {
    mm_device_t *device = malloc(sizeof *device);
    device->raw_device = args.raw_device;
    device->byte_reader = args.br ?: null_byte_reader;
    device->word_reader = args.wr ?: null_word_reader;
    device->byte_writer = args.bw ?: null_byte_writer;
    device->word_writer = args.ww ?: null_word_writer;
    return device;
}

void mmu_add(mmu_t* map, mm_mapping_t *mapping) {
    size_t num_bytes = sizeof(mm_mapping_t *) * map->num_devices;

    memmove(map->mapping + 1, map->mapping, num_bytes);

    map->mapping[0] = mapping;
    map->num_devices++;
}

void mmu_rem(mmu_t* map, mm_mapping_t *mapping) {
    for (int i = 0; i < map->num_devices; i++) {
        if (map->mapping[i] != mapping)
            continue;

        size_t num_bytes = sizeof(mm_mapping_t *) * (map->num_devices - i - 1);

        memmove(map->mapping + i, map->mapping + i + 1, num_bytes);
        map->num_devices--;
        return;
    }
}

static mm_mapping_t * get_mapping_at(mmu_t* map, int pos);

byte mmu_read_byte(mmu_t* map, int pos) {
    mm_mapping_t *mapping = get_mapping_at(map, pos);
    return mapping->device->byte_reader(mapping->device->raw_device, pos);
}

word mmu_read_word(mmu_t* map, int pos) {
    mm_mapping_t *mapping = get_mapping_at(map, pos);
    return mapping->device->word_reader(mapping->device->raw_device, pos);
}

void mmu_write_byte(mmu_t* map, int pos, byte byte) {
    mm_mapping_t *mapping = get_mapping_at(map, pos);
    mapping->device->byte_writer(mapping->device->raw_device, pos, byte);
}

void mmu_write_word(mmu_t* map, int pos, word word) {
    mm_mapping_t *mapping = get_mapping_at(map, pos);
    mapping->device->word_writer(mapping->device->raw_device, pos, word);
}

static mm_mapping_t* get_mapping_at(mmu_t* map, int pos) {
    for (int i = 0; i < map->num_devices; i++) {
        if (map->mapping[i]->start <= pos && pos <= map->mapping[i]->end) {
            return map->mapping[i];
        }
    }
    return NULL; // Should never happen since mmu is the base mapping & takes the entire range
}

void *mmu_get_root_device(mmu_t* mmu) {
    return mmu->mapping[mmu->num_devices - 1]->device->raw_device;
}
