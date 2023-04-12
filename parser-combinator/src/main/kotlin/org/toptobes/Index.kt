package org.toptobes

import org.toptobes.vm.*
import java.nio.ByteBuffer

fun main() {
    val mem = ByteBuffer.allocate(1024)
    val cpu = Cpu(mem)

    mem.put(MOV_IMM_2_REG)
    mem.reg(r.ax)
    mem.imm(0x00, 0x01)

    mem.put(MOV_IMM_2_REG)
    mem.reg(r.bx)
    mem.imm(0x00, 0x01)

    mem.put(ADD_REG_REG)
    mem.reg(r.ax)
    mem.put(r.bx.code)

    mem.put(MOV_REG_2_MEM)
    mem.imm(0x00, 0x00)
    mem.reg(r.ax)

    mem.put(MOV_MEM_2_REG)
    mem.reg(r.dx)
    mem.imm(0x00, 0x00)

    mem.put(HALT)

    cpu.cycle(debug = true)
}

fun Memory.reg(reg: Register) {
    put(reg.code)
}

fun Memory.imm(byte1: Byte, byte2: Byte) {
    put(byte1)
    put(byte2)
}
