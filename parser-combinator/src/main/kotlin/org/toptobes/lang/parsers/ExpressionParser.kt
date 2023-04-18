//package org.toptobes.lang.parsers
//
//import org.toptobes.lang.*
//import org.toptobes.lang.parsers.EvalStates.*
//import org.toptobes.parsercombinator.contextual
//import org.toptobes.parsercombinator.impls.any
//import org.toptobes.parsercombinator.impls.str
//import org.toptobes.parsercombinator.unaryMinus
//import java.util.*
//
//val expr = expr()
//    .tagStruct("EXPR")
//
//private val operator = any(str('+'), str('-'), str('*'))
//    .tagString("OPERAND")
//
//private val operand = any(/*variable, */imm8)
//    .tagStruct("OPERATOR")
//
//private enum class EvalStates { OpenedBracket, ClosedBracket, OperatorOrClosedBracket, OperandOrOpenedBracket }
//
//private fun expr(isAddress: Boolean = false) = contextual { ctx ->
//    var state = OperandOrOpenedBracket
//    val stack = Stack<MutableList<TaggedValue>>()
//    lateinit var currentList: TaggedValue
//
//    stack.push(mutableListOf())
//
//    val openBracketParser = if (isAddress) {
//        -any(str('('), str('['))
//    } else {
//        -str('(')
//    }
//
//    val closedBracketParser = if (isAddress) {
//        -any(str(')'), str(']'))
//    } else {
//        -str(')')
//    }
//
//    (ctx parse openBracketParser) ?: error("No initial opening bracket")
//
//    while (true) { when (state) {
//        OpenedBracket -> {
//            val newList = mutableListOf<TaggedValue>()
//            stack.peek().add("OPERATION" tagList newList)
//            stack.push(newList)
//
//            (ctx parse -str('(')) ?: error("No opening bracket")
//
//            state = OperandOrOpenedBracket
//        }
//        ClosedBracket -> {
//            currentList = "OPERATION" tagList stack.pop()
//
//            if (stack.isEmpty()) {
//                ctx parse closedBracketParser
//                break
//            } else {
//                ctx parse -str(')')
//            }?: error("No closing bracket")
//
//            state = OperatorOrClosedBracket
//        }
//        OperatorOrClosedBracket -> {
//            if (ctx.tryParse(openBracketParser) != null) {
//                error("Unexpected opening bracket")
//            }
//
//            if (ctx.tryParse(closedBracketParser) != null) {
//                state = ClosedBracket
//            } else {
//                state = OperandOrOpenedBracket
//                stack.peek().add((ctx parse -operator) ?: error("Unexpected operator"))
//            }
//        }
//        OperandOrOpenedBracket -> {
//            if (ctx.tryParse(closedBracketParser) != null) {
//                error("Unexpected closing bracket")
//            }
//
//            if (ctx.tryParse(openBracketParser) != null) {
//                state = OpenedBracket
//            } else {
//                state = OperatorOrClosedBracket
//                stack.peek().add((ctx parse -operand) ?: error("Unexpected operand"))
//            }
//        }
//    }}
//
//    success(currentList)
//}.map(::correctOrderOfOperations)
//
//fun correctOrderOfOperations(expr: TaggedValue): TaggedValue {
//    if (expr.tag != "OPERATION") {
//        return expr
//    }
//
//    if (expr.valAsList.size == 1) {
//        return expr.valAsList.first()
//    }
//
//    val mutExpr = expr.valAsList.toMutableList()
//
//    for (i in (1 until mutExpr.size - 1 step 2).reversed()) {
//        if (mutExpr[i].valAsString == "*") {
//            val lop = mutExpr[i - 1].run(::correctOrderOfOperations)
//            val rop = mutExpr[i + 1].run(::correctOrderOfOperations)
//
//            mutExpr[i] = "OPERATION" tagList listOf(lop, mutExpr[i], rop)
//            mutExpr.removeAt(i + 1)
//            mutExpr.removeAt(i - 1)
//        }
//    }
//
//    return "OPERATION" tagList mutExpr
//}
