@file:Suppress("MemberVisibilityCanBePrivate", "EnumEntryName")

package org.toptobes.vm

import kotlin.system.exitProcess

class Cpu(val memory: Memory) {
    val registerMem = makeMemory(Register.size * 2)

    // -- REGISTER STUFF --

    operator fun get(register: Register): Word {
        return getRegByOffset(register.offset)
    }

    fun getRegByOffset(offset: Int): Short {
        return registerMem.getShort(offset)
    }

    operator fun set(register: Register, word: Word) {
        setRegByOffset(register.offset, word)
    }

    operator fun set(register: Register, byte: Byte) {
        setRegByOffset(register.offset, byte.toShort())
    }

    fun setRegByOffset(offset: Int, byte: Byte) {
        setRegByOffset(offset, byte.toShort())
    }

    fun setRegByOffset(offset: Int, word: Word) {
        registerMem.putShort(offset, word)
    }

    // -- FETCH STUFF --

    fun fetchRegOffsetByte(): Int {
        return fetchByte() * 2
    }

    fun fetchByte(): Byte {
        val instructionAddress = this[r.ip]++.toInt()
        return memory[instructionAddress]
    }

    fun fetchWord(): Word {
        val instructionAddress = this[r.ip].toInt()
        this[r.ip] = (instructionAddress + 2).toByte()
        return memory.getShort(instructionAddress)
    }

    // -- EXECUTION --

    fun execute(instruction: Byte): Any = when(instruction) {
        MOV_IMM_2_REG -> {
            val reg = fetchRegOffsetByte()
            val imm = fetchWord()
            setRegByOffset(reg, imm)
        }

        MOV_REG_2_REG -> {
            val to   = fetchRegOffsetByte()
            val from = fetchRegOffsetByte()
            setRegByOffset(from, getRegByOffset(to))
        }

        MOV_MEM_2_REG -> {
            val reg = fetchRegOffsetByte()
            val adr = fetchWord()
            setRegByOffset(reg, memory.getShort(adr.toInt()))
        }

        MOV_REG_2_MEM -> {
            val adr = fetchWord()
            val reg = fetchRegOffsetByte()
            memory.putShort(adr.toInt(), getRegByOffset(reg))
        }

        ADD_REG_REG -> {
            val r1 = registerMem.getShort(fetchRegOffsetByte())
            val r2 = registerMem.getShort(fetchRegOffsetByte())
            this[r.acc] = (r1 + r2).toShort()
        }

        JMP_NOT_EQL -> {
            val value = fetchWord()
            val adr   = fetchWord()

            if (value != this[r.acc]) {
                this[r.ip] = adr
            } else Unit
        }

        HALT -> {
            exitProcess(0)
        }

        else -> throw IllegalArgumentException("Invalid instruction $instruction")
    }

    // -- DEBUG --

    fun viewMemAt(addr: Int) {
        println(memory[addr])
    }

    fun step() {
        val instruction = fetchByte()
        execute(instruction)
    }

    fun cycle(debug: Boolean = false) {
        while (true) {
            step()

            if (!debug) continue

            Register.values().forEach { print("$it: 0x" + this[it].toString(16).padStart(4, '0') + ", ") }
            println()
        }
    }
}

typealias r = Register

enum class Register {
    ip, acc, ax, bx, cx, dx;

    val code   = ordinal.toByte()
    val offset = ordinal * 2

    companion object {
        const val size = 6
    }
}

const val MOV_IMM_2_REG = 0x10.toByte()
const val MOV_REG_2_REG = 0x11.toByte()
const val MOV_REG_2_MEM = 0x12.toByte()
const val MOV_MEM_2_REG = 0x13.toByte()
const val ADD_REG_REG   = 0x14.toByte()
const val JMP_NOT_EQL   = 0x15.toByte()
const val HALT          = 0x16.toByte()
