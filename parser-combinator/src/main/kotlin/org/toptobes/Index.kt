package org.toptobes

import org.toptobes.lang.compile
import org.toptobes.lang.utils.ifLeft
import org.toptobes.lang.utils.ifRight
import java.io.File

fun main() {
    val instructions = File("../playground")
        .readText()

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
