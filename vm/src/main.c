#include "vm/cpu/cpu.h"
#include "vm/memory/mem.h"

int main() {
    // Gives CLion a second to initialize the
    // terminal to stop display errors
    for (int i = 0; i < INT16_MAX << 8; i++);

    mem_t *mem = mem_new(1024);
    cpu_t *cpu = cpu_new(mem);

    cpu_write_byte(call_imm);
    cpu_write_word(0x0004);

    cpu_write_byte(halt);

    cpu_write_byte(mov16_imm_reg);
    cpu_write_byte(ax);
    cpu_write_word(0x0001);

    cpu_write_byte(mov16_imm_reg);
    cpu_write_byte(bx);
    cpu_write_word(0x0002);

    cpu_write_byte(add16_reg_reg);
    cpu_write_byte(ax);
    cpu_write_byte(bx);

    cpu_write_byte(ret);

    cpu->registers[ip] = 0;

    printf("1 + 2 = %d", cpu_run(cpu));

    return cpu->flags.zf;
}
