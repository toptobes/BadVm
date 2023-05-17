package org.toptobes

import org.toptobes.lang.compile
import org.toptobes.parsercombinator.DescriptiveParsingException
import java.io.File

fun main() {
    try {
        val code = compile("""
        imm word PRINTER = 0x400
        imm word READER = PRINTER
        
        macro PrintLn reg str =
          | byte RandName(1) = str, 10, 0
          | mov reg, &RandName(1)
          | mov @<word ptr PRINTER>, reg
        
        type Word =| word word
        Word myWord = Word { 0 }
        
        _start:
            mov <Word ptr cx>.word, ax
        
            PrintLn(cx, str: "Guess a number in 1..100")
            mov dx, 16
            
            LoopHead:
                mov cx, @<word ptr READER>
                call closeness
                jmp LoopHead
        
        closeness:
            cmp cx, dx
            jgt GreaterThan
            jlt LessThan
            
            PrintLn(cx, "You got it!")
            halt
            
            LessThan:
                PrintLn(cx, "Higher...")
                ret
                
            GreaterThan:
                PrintLn(cx, "Lower...")
                ret
        """.trimIndent())

        println(code)

        File("../out").bufferedWriter().use { writer ->
            code?.let { writer.write(it.joinToString(",")) }
        }
    } catch (e: DescriptiveParsingException) {
        println(e.message)
    }
}
