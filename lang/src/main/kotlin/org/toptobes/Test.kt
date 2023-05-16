package org.toptobes

import org.toptobes.lang.ast.WordIntrp
import org.toptobes.lang.codegen.encode
import org.toptobes.lang.parsing.codeParser
import org.toptobes.lang.preprocessor.findLabels
import org.toptobes.lang.preprocessor.findMacros
import org.toptobes.lang.preprocessor.replaceMacros
import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.prettyString
import org.toptobes.parsercombinator.DescriptiveParsingException
import org.toptobes.parsercombinator.isOkay
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

const val RESERVED_MEM_SIZE = 2

private fun compile(str: String): List<Byte>? {
    val (newStr, macros) = findMacros(str)
    val newStr2 = replaceMacros(macros, newStr)
    val labels = findLabels(newStr2)
    val ast = codeParser(newStr2, labels)

    return if (ast.isOkay()) {
        println(ast.prettyString())
        encode(ast.result, ast.symbols, ast.allocations)
    } else {
        println(ast.error)
        null
    }
}
