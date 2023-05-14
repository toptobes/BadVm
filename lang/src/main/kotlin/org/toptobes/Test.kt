package org.toptobes

import org.toptobes.lang.codegen.encode
import org.toptobes.lang.parsing.codeParser
import org.toptobes.lang.parsing.instructions
import org.toptobes.lang.preprocessor.findMacros
import org.toptobes.lang.preprocessor.replaceMacros
import org.toptobes.parsercombinator.DescriptiveParsingException
import org.toptobes.parsercombinator.isOkay
import java.io.Serializable

fun main() {
    try {
        val code = compile("""
            imm word PRINT_ADDR = 1024
            
            type AddressHolder =
              | word addr
            
            imm byte read_addr_as_byte_arr = { 0x04, 0x00 }
            imm AddressHolder READ = AddressHolder { addr: ...read_addr_as_byte_arr }
            
            macro Print reg str_addr =
              | mov reg, str_addr
              | mov @{word ptr PRINT_ADDR}, reg
            
            byte won = 0
            
            _start:
                byte guess_msg = "Guess a number in 1..10", 0
                Print(cx, str_addr: &guess_msg)
                
                LoopHead:
                    mov ax, @{word ptr READ.addr}
                    cmp ax, 4
                    jne LoopHead
                    
                byte win_msg = "You got it!", 0
                Print(cx, &guess_msg)
                
                mov al, 1
                mov @won, al
        """.trimIndent())

        println(code)
    } catch (e: DescriptiveParsingException) {
        println(e.message)
    }
}

private fun compile(str: String): List<Byte>? {
    val (newStr, macros) = findMacros(str)
    val newStr2 = replaceMacros(macros, newStr)
    val ast = codeParser(newStr2)

    return if (ast.isOkay()) {
        println(ast.result)
        encode(ast.result)
    } else {
        println(ast.error)
        null
    }
}
