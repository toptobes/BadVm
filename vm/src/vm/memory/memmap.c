#include <malloc.h>
#include "memmap.h"

mm_mapping *mm_mapping_new(mm_device *device, size_t start, size_t end) {
    mm_mapping *mapping = malloc(sizeof *mapping);
    mapping->device = device;
    mapping->start  = start;
    mapping->end    = end;
    mapping->is_absolute = false;
    return mapping;
}

void mm_mapping_add(mem_map_t* mem, mm_mapping *mapping) {
    ;
}

void mm_mapping_rem(mem_map_t* mem, mm_mapping *mapping) {
    ;
}
