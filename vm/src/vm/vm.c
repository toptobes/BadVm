#include "vm.h"

static mmu_t *mem_init(mem_t *mem);

void vm_init(vm_t *vm, word mem_size) {
    mem_t *mem = mem_new(mem_size);
    mmu_t *mmap = mem_init(mem);

    vm->cpu = cpu_new(mmap);
    vm->mmu = mmap;
}

void vm_inject_code(vm_t *vm, byte *code, size_t size) {
    for (int i = 0; i < size; i++) {
        mmu_write_byte(vm->mmu, vm->cpu->registers[ip] + i, code[i]);
    }
}

int vm_run(vm_t *vm) {
    return cpu_run(vm->cpu);
}

void vm_add_mem_mapping(vm_t *vm, mm_device_t *device, int start, int end) {
    mm_mapping_t *mapping = mm_mapping_new(device, start, end);
    mmu_add(vm->mmu, mapping);
}

static mmu_t *mem_init(mem_t *mem) {
    mm_device_t *device = malloc(sizeof *device);
    device->raw_device = mem;
    device->byte_reader = (byte (*)(void *, int)) mem_read_byte;
    device->word_reader = (word (*)(void *, int)) mem_read_word;
    device->byte_writer = (void (*)(void *, int, byte)) mem_write_byte;
    device->word_writer = (void (*)(void *, int, word)) mem_write_word;

    mm_mapping_t *mapping = mm_mapping_new(device, 0, mem->size);
    mmu_t *mmap = mmu_new();
    mmu_add(mmap, mapping);

    return mmap;
}
