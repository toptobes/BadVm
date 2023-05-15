#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(jne_mem16) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jeq_mem16) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 1) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jgt_mem16) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 0 && cpu->flags.sf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jlt_mem16) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 0 && cpu->flags.sf == 1) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jge_mem16) {
    word adr = cpu_read_word();

    if (cpu->flags.sf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jle_mem16) {
    word adr = cpu_read_word();

    if (cpu->flags.zf ^ cpu->flags.sf) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jmp_mem16) {
    word adr = cpu_read_word();
    cpu_reg16(ip) = adr;
}

OPCODE_IMPL(jne_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmu_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jeq_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmu_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 1) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jgt_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmu_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 0 && cpu->flags.sf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jlt_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmu_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 0 && cpu->flags.sf == 1) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jge_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmu_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.sf == 0) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jle_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmu_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf ^ cpu->flags.sf) {
        cpu_reg16(ip) = adr;
    }
}

OPCODE_IMPL(jmp_ptr) {
    byte reg = cpu_read_byte();
    word adr = mmu_read_word(cpu->mmap, cpu_reg16(reg));
    cpu_reg16(ip) = adr;
}

void init_jump_opcodes(struct cpu_t *cpu) {
    ASSIGN_OPCODE(jne_mem16);
    ASSIGN_OPCODE(jeq_mem16);
    ASSIGN_OPCODE(jgt_mem16);
    ASSIGN_OPCODE(jlt_mem16);
    ASSIGN_OPCODE(jge_mem16);
    ASSIGN_OPCODE(jle_mem16);
    ASSIGN_OPCODE(jmp_mem16);
    ASSIGN_OPCODE(jne_ptr);
    ASSIGN_OPCODE(jeq_ptr);
    ASSIGN_OPCODE(jgt_ptr);
    ASSIGN_OPCODE(jlt_ptr);
    ASSIGN_OPCODE(jge_ptr);
    ASSIGN_OPCODE(jle_ptr);
    ASSIGN_OPCODE(jmp_ptr);
}
