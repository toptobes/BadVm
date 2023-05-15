#include "vm/cpu/cpu.h"
#include "vm/memory/mmu.h"
#include "vm/vm.h"

static void io_write_byte(void* device, int _, byte byte) {
    mem_t *mem = mmu_get_root_device(device);
    printf((const char *) mem->mem + byte, byte);
}

static void io_write_word(void* device, int _, word word) {
    mem_t *mem = mmu_get_root_device(device);
    printf((const char *) mem->mem + word, word);
}

static byte io_read_byte(void* device, int _) {
    byte b;
    scanf("%hhd", &b);
    return b;
}

static word io_read_word(void* device, int _) {
    word w;
    scanf("%hu", &w);
    return w;
}

int main() {
    vm_t vm;
    vm_init(&vm, -1);

    mm_device_t *io = mm_device_new(.bw = &io_write_byte, .ww = &io_write_word, .br = &io_read_byte, .wr = &io_read_word, .raw_device = vm.mmu);
    vm_add_mem_mapping(&vm, io, 0x400, 0x400);

    byte code[] = {
        #include "../../out"
    };
    size_t code_size = (sizeof code / sizeof *code);

    vm_inject_code(&vm, code, code_size);
    return vm_run(&vm);
}
