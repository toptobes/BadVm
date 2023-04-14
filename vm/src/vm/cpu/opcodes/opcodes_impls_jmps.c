#include "opcodes.h"
#include "../cpu.h"

OPCODE_IMPL(jne_imm) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 0) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jeq_imm) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 1) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jgt_imm) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 0 && cpu->flags.sf == 0) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jlt_imm) {
    word adr = cpu_read_word();

    if (cpu->flags.zf == 0 && cpu->flags.sf == 1) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jge_imm) {
    word adr = cpu_read_word();

    if (cpu->flags.sf == 0) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jle_imm) {
    word adr = cpu_read_word();

    if (cpu->flags.zf ^ cpu->flags.sf) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jne_reg) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 0) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jeq_reg) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 1) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jgt_reg) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 0 && cpu->flags.sf == 0) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jlt_reg) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf == 0 && cpu->flags.sf == 1) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jge_reg) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.sf == 0) {
        cpu->registers[ip] = adr;
    }
}

OPCODE_IMPL(jle_reg) {
    byte reg = cpu_read_byte();
    word adr = mmap_read_word(cpu->mmap, cpu_reg16(reg));

    if (cpu->flags.zf ^ cpu->flags.sf) {
        cpu->registers[ip] = adr;
    }
}

void init_jump_opcodes(struct cpu_t *cpu) {
    cpu->opcodes[jne_imm] = jne_imm_impl;
    cpu->opcodes[jeq_imm] = jeq_imm_impl;
    cpu->opcodes[jgt_imm] = jgt_imm_impl;
    cpu->opcodes[jlt_imm] = jlt_imm_impl;
    cpu->opcodes[jge_imm] = jge_imm_impl;
    cpu->opcodes[jle_imm] = jle_imm_impl;
    cpu->opcodes[jne_reg] = jne_reg_impl;
    cpu->opcodes[jeq_reg] = jeq_reg_impl;
    cpu->opcodes[jgt_reg] = jgt_reg_impl;
    cpu->opcodes[jlt_reg] = jlt_reg_impl;
    cpu->opcodes[jge_reg] = jge_reg_impl;
    cpu->opcodes[jle_reg] = jle_reg_impl;
}
