#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(jne_mem) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jeq_mem) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 1) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jgt_mem) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 0 && cpu->flags.sf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jlt_mem) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 0 && cpu->flags.sf == 1) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jge_mem) {
    word adr = cpu_read_word();

    if (cpu->flags.sf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jle_mem) {
    word adr = cpu_read_word();

    if (cpu->flags.zf ^ cpu->flags.sf) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jmp_mem) {
    word adr = cpu_read_word();
    cpu_reg16(ip) = adr;
}

OPCODE_IMPL(jne_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jeq_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 1) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jgt_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 0 && cpu->flags.sf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jlt_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 0 && cpu->flags.sf == 1) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jge_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.sf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jle_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf ^ cpu->flags.sf) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jmp_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));
    cpu_reg16(ip) = adr;
}

void init_jump_opcodes(struct cpu_t *cpu) {
    ASSIGN_OPCODE(jne_mem, jne_mem_impl);
    ASSIGN_OPCODE(jeq_mem, jeq_mem_impl);
    ASSIGN_OPCODE(jgt_mem, jgt_mem_impl);
    ASSIGN_OPCODE(jlt_mem, jlt_mem_impl);
    ASSIGN_OPCODE(jge_mem, jge_mem_impl);
    ASSIGN_OPCODE(jle_mem, jle_mem_impl);
    ASSIGN_OPCODE(jmp_mem, jmp_mem_impl);
    ASSIGN_OPCODE(jne_ptr, jne_ptr_impl);
    ASSIGN_OPCODE(jeq_ptr, jeq_ptr_impl);
    ASSIGN_OPCODE(jgt_ptr, jgt_ptr_impl);
    ASSIGN_OPCODE(jlt_ptr, jlt_ptr_impl);
    ASSIGN_OPCODE(jge_ptr, jge_ptr_impl);
    ASSIGN_OPCODE(jle_ptr, jle_ptr_impl);
    ASSIGN_OPCODE(jmp_ptr, jmp_ptr_impl);
}
