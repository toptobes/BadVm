package org.toptobes

import org.toptobes.lang.parsing.instructionParser

fun main() {
    instructionParser.log("mov ax, bx")
}

/**
 * ```
 * // myWord is replaced with "3" wherever myWord is used
 * // Can not be dereferenced
 * // i.e. 'mov rax, myWord' -> 'mov rax, 3'
 * imm word myWord = 3
 *
 * // Illegal
 * imm word myWord [numBytes] [initializer]
 * imm word myWord = { ... }
 *
 * // myWord is replaced with the address to the location of myWord
 * // and actually goes to the address when dereferenced
 * // i.e. 'mov ax, myWord ' -> 'mov ax, 0xff  '
 * // or   'mov ax, @myWord' -> 'mov ax, [0xff]'
 * word myWord = 3
 *
 * // myWord can be embedded in other declarations
 * // Can not be dereferenced
 * // Can not be used in an instruction
 * // i.e. 'word myOtherWord = { 0, ...myWord, 5 }`
 * embed word myWord = { 1, 2, 3 }
 *
 * // Legal
 * embed word myWord [numBytes] [initializer]
 * embed word myWord = { ... }
 *
 * // -------------------------------------------------------------
 *
 * type Byte =
 *   | byte b
 *
 * type Bytes =
 *   | byte b1
 *   | byte b2
 *
 * type Word =
 *   | word w
 *
 * type Words =
 *   | word w1
 *   | word w2
 *
 * type Node =
 *   | Word w
 *   | addr Node next
 *
 * // -------------------------------------------------------------
 *
 * // myByte is replaced with the (address + offset of field) of myByte
 * // and goes to the (address + offset of field) when dereferenced
 * // i.e. 'mov al, myByte.b ' -> 'mov al, 0xff + 0  '
 * // i.e. 'mov al, @myByte.b' -> 'mov al, [0xff + 0]'
 * Byte myByte = Byte{3}
 *
 * // myByte is replaced with the value of myByte @ the field offset
 * // Can not be dereferenced
 * // i.e. 'mov al, myByte.b ' -> 'mov al, 3'
 * imm Byte myByte = Byte{3}
 *
 * // myWords is replaced with the value of myByte @ the field offset
 * // Can not be dereferenced
 * // i.e. 'mov ax, myWords.w2 ' -> 'mov al, 8'
 * imm Words myWords = Words { w1: 4, w2: 8 }
 *
 * // myWord can be used directly inside of another type
 * // Can not be dereferenced
 * // Can not be used in an instruction
 * // i.e. 'Node { w: ...myWord, next: ? }
 * embed Word myWord = Word{1}
 *
 * // Legal
 * Node myNode1 = Node { Word{1}, next: ? }
 * Node myNode2 = Node { Word{2}, next: myNode1 }
 *
 * // Illegal
 * Node myNode2 = Node { Word{2}, next: @myNode1 }
 *
 * // Illegal
 * Word mN1Addr = Word { myNode1 }
 * Node myNode2 = Node { Word{2}, next: @mN1Addr.w }
 *
 * // Legal
 * embed Word mN1Addr = Word { myNode1 }
 * Node myNode2 = Node { Word{2}, next: ...mN1Addr }
 *
 * // Legal
 * imm Word mN1Addr = Word { myNode1 }
 * Node myNode2 = Node { Word{2}, next: mN1Addr.w }
 */
object Doc
