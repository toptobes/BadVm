#include "vm/cpu/cpu.h"
#include "vm/memory/memmap.h"
#include "vm/vm.h"

static void char_io_write_byte(void* device, int pos, byte byte) {
    printf("%c", byte);
}

static void char_io_write_word(void* device, int pos, word word) {
    printf("%c", word);
}

int main() {
    vm_t vm;
    vm_init(&vm, -1);

    mm_device_t *char_io = mm_device_new(.bw = &char_io_write_byte, .ww = &char_io_write_word);
    vm_add_mem_mapping(&vm, char_io, 400, 400);

    byte code[] = {
        #include "../../out"
    };
    size_t code_size = (sizeof code / sizeof *code);

    vm_inject_code(&vm, code, code_size);
    return vm_run(&vm);
}
