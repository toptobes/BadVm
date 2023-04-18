package org.toptobes

import org.toptobes.lang.compile
import org.toptobes.utils.ifLeft
import org.toptobes.utils.ifRight
import java.io.File

fun main() {
    val instructions = File("../playground")
        .readText()

//    val instructions = """
//    const word TWO = 3
//
//    _start:
//        mov ax, ${'$'}TWO
//        LoopHead:
//            sub ax, 1
//            cmp ax, 0
//            jne LoopHead
//        stop
//    """.trimIndent()

    val maybeBytecode = compile(instructions)

    maybeBytecode.ifLeft { err ->
        println(err.rootCause())
    }

    maybeBytecode.ifRight { bytecode ->
        val bytecodeStr = bytecode.joinToString(", ")
        println("{ $bytecodeStr }")
        File("../out").writeText(bytecodeStr)
    }
}
