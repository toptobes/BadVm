package org.toptobes

import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.*
import org.toptobes.parsercombinator.rangeTo
import org.toptobes.parsercombinator.unaryMinus

sealed interface Instruction
class Mov

sealed interface Argument
class Reg16(val name: String)
class Reg8(val name: String)
class Imm16(val value: Int)
class Imm8(val value: Int)
class Mem(val value: Int)

val mov = str("mov")
val reg16 = any(str("rax"), str("rbx"), str("rcx"), str("rdx"))
val reg8  = any(str("ah"), str("al"), str("bh"), str("bl"), str("ch"), str("cl"), str("dh"), str("dl"))
val imm16 = upto(16, digits)
val imm8  = upto(8, digits)
val mem = between.squareBrackets(any(reg16, imm16))

fun main() {
    val str = "mov rax, rbx"

    contextual(str) { ctx ->
        val instruction = (ctx parse -mov) ?: return@contextual

        val arg1 = ctx parse any(
            reg16 .. { Reg16(it) },
            reg8  .. { Reg8(it)  },
            imm16  .. { Reg8(it)  },
            imm8  .. { Reg8(it)  },
            mem  .. { Reg8(it)  },
        )
    }
}
