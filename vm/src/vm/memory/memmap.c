#include <malloc.h>
#include <string.h>
#include "memmap.h"

mem_map_t* mem_map_new() {
    mem_map_t *map = malloc(sizeof *map);
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

void mmap_add(mem_map_t* map, mm_mapping_t *mapping) {
    size_t num_bytes = sizeof(mm_mapping_t *) * map->num_devices;

    memmove(map->mapping + 1, map->mapping, num_bytes);

    map->mapping[0] = mapping;
    map->num_devices++;
}

void mmap_rem(mem_map_t* map, mm_mapping_t *mapping) {
    for (int i = 0; i < map->num_devices; i++) {
        if (map->mapping[i] != mapping)
            continue;

        size_t num_bytes = sizeof(mm_mapping_t *) * (map->num_devices - i - 1);

        memmove(map->mapping + i, map->mapping + i + 1, num_bytes);
        map->num_devices--;
        return;
    }
}

static mm_mapping_t * get_mapping_at(mem_map_t* map, int pos);

byte mmap_read_byte(mem_map_t* map, int pos) {
    mm_mapping_t *mapping = get_mapping_at(map, pos);
    return mapping->device->read_byte(mapping->device->raw_device, pos);
}

word mmap_read_word(mem_map_t* map, int pos) {
    mm_mapping_t *mapping = get_mapping_at(map, pos);
    return mapping->device->read_word(mapping->device->raw_device, pos);
}

void mmap_write_byte(mem_map_t* map, int pos, byte byte) {
    mm_mapping_t *mapping = get_mapping_at(map, pos);
    mapping->device->write_byte(mapping->device->raw_device, pos, byte);
}

void mmap_write_word(mem_map_t* map, int pos, word word) {
    mm_mapping_t *mapping = get_mapping_at(map, pos);
    mapping->device->write_word(mapping->device->raw_device, pos, word);
}

static mm_mapping_t* get_mapping_at(mem_map_t* map, int pos) {
    for (int i = 0; i < map->num_devices; i++) {
        if (map->mapping[i]->start <= pos && pos <= map->mapping[i]->end) {
            return map->mapping[i];
        }
    }
    return NULL; // Should never happen since mmap is the base mapping & takes the entire range
}
