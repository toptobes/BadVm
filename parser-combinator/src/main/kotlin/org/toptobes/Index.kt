package org.toptobes

import org.toptobes.lang.compile
import org.toptobes.lang.utils.ifLeft
import org.toptobes.lang.utils.ifRight
import java.io.File

fun main() {
    val instructions = """
        #=
        You can use 'declare type' to declare but not define a struct for
        circular reference purposes. It can be defined for real later.
        =#
        declare type SimplyDeclared

        # Types can hold bytes, words (shorts), or other types
        type ThreeChars =
          | byte a
          | byte b
          | byte c

        type Mom =
          | byte m1
          | byte o
          | byte m2

        # You can use '&' to paste all of the fields from one type into another
        type Hi =
          | byte h
          | byte i
          | ThreeChars chars
          & Mom

        # Both positional and named constructors supported
        Hi r1 = Hi{0x86, 01101001b, ThreeChars {  b = 98, a = 'a', c=99,}, 'm', 'o', 'm'}
        
        # Or a wildcard to declare a 'zeroed out' struct
        Hi r2 = ?

        const word PRINT_CHR = 400

        _start:
            mov ax, &r1
            mov &PRINT_CHR, ax
            inc ax
            mov &PRINT_CHR, ax
            inc ax
            mov &PRINT_CHR, ax
    """.trimIndent()

//    val instructions = File("../playground")
//        .readText()

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
