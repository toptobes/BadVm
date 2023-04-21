package org.toptobes

import org.toptobes.lang.compile
import org.toptobes.lang.utils.ifLeft
import org.toptobes.lang.utils.ifRight

fun main() {
//    val instructions = """
//        declare type End
//
//        type ThreeChars =
//          | byte a
//          | byte b
//          | byte c
//
//        type Mom =
//          | byte m1
//          | byte o
//          | byte m2
//
//        type Hi =
//          | byte h
//          | byte i
//          | ThreeChars chars
//          & Mom
//          | End end
//
//        type End =
//          | byte end
//
//        Hi r1 = Hi{0x68, 01101001b, ThreeChars { b: 'a', a : 98, c:99,}, 'm', 'o', 'm', End{0}}
//
//        Hi r2 = ?
//
//        const word PRINT_CHR = 400
//
//        _start:
//            mov cx, @PRINT_CHR
//            mov al, @r1.chars.a
//            xor bx, bx
//
//            LoopHead:
//                mov bl, [ax]
//                inc ax
//
//                cmp bx, 0
//                jeq LoopHeadEnd
//
//                mov &PRINT_CHR, bx
//                jmp LoopHead
//            LoopHeadEnd:
//    """.trimIndent()

//    val instructions = java.io.File("../playground")
//        .readText()

    val instructions = """
        type Node =
          | addr Node left
          | word data
          | addr Node right

        imm Node n2 = Node { left: ?, data: 2, right: ?   }
        Node n1 = Node { left: ?, data: 1, right: n2 }
        
        type Tree =
          | Node root

        Tree tree = Tree { n1 }

        _start:
            mov ax, @n1.@right
            stop
    """.trimIndent()

    val maybeBytecode = compile(instructions)

    maybeBytecode.ifLeft { err ->
        println(err)
    }

    maybeBytecode.ifRight { bytecode ->
        val bytecodeStr = bytecode.joinToString(", ")
        println("{ $bytecodeStr }")
//        File("../out").writeText(bytecodeStr)
    }
}
