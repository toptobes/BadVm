package org.toptobes

import org.toptobes.lang.nodes.*
import org.toptobes.lang.parsers.identifier
import org.toptobes.lang.utils.StatefulParsingException
import org.toptobes.lang.utils.StatelessParsingException
import org.toptobes.lang.utils.Word
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.sepBy
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.unaryMinus

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
 * // Legal
 * Word mN1Addr = Word { @myNode1 }
 * Node myNode2 = Node { Word{2}, next: @mN1Addr.w }
 */
fun main() {
    val H = DefinedType("H", listOf(TypeDefinitionFieldByte("s")))
    val h = TypeInstance("h", H, listOf(ByteVarDefinition("s", 1)))

    val vars = MutNodes().apply {
        typeDefs += H
        varDefs  += h
    }

    try {
        println(varUsage(vars)("@h.s"))
    } catch (e: StatefulParsingException) {
        println(e.message!!)
    }
}

fun varUsage(nodes: Nodes) = contextual { ctx ->
    val isDeref = ctx canTryParse -str('@')

    val cascadingNames = ctx parse sepBy.periods(-identifier, allowTrailingSep = false) or ccrash("No identifier found for variable usage")
    val firstName = cascadingNames[0]
    val first = nodes.varDefs[firstName]                                                or ccrash("No identifier with name ${cascadingNames[0]}")

    val (_, variable) = cascadingNames.drop(1).fold(firstName to first) { (prevName, variable), name ->
        if (variable !is TypeInstance) {
            crash("Trying to call $name on non-defined-type $prevName")
        }

        val next = variable.fields.firstOrNull { it.identifier == name }                or ccrash("No identifier with name $name")
        next.identifier to next
    }

    if (variable !is StaticDefinition) {
        throw StatelessParsingException("$variable is not a static definition")
    }

    when (variable) {
        is ByteVarDefinition -> when {
            isDeref -> crash("Can not deref byte variable $variable")
            else -> ByteVariable(variable.identifier, variable.byte)
        }
        is WordVarDefinition -> when {
            isDeref -> AddrVariable(variable.identifier)
            else -> WordVariable(variable.identifier, variable.word)
        }
        is TypeInstance -> when (variable.varType) {
            is Immediate -> when (variable.size) {
                1 -> ByteVariable(variable.identifier, variable.toBytes()[0])
                2 -> WordVariable(variable.identifier, variable.toBytes().toWord())
                else -> crash("Can not have immediate of ")
            }
            is Embedded -> {
                EmbeddedBytesVariable(variable.identifier, variable.toBytes())
            }
            else -> crash("Can not directly use a non-imm type instance")
        }
    }

    success(variable)
}

fun List<Byte>.toWord(): Word {
    return ((this[0].toInt() shl 8) + this[1]).toShort()
}
