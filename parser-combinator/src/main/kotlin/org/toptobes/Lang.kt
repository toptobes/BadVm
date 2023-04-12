package org.toptobes

import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.between
import org.toptobes.parsercombinator.impls.digits
import org.toptobes.parsercombinator.impls.regex
import org.toptobes.utils.*
import org.toptobes.utils.Number

fun interpret(expression: String): Either<ErrorResult, Int> {
    val tokens = parser(expression)

    if (tokens.isErrored) {
        return Either.Left(tokens.error!!)
    }

    return Either.Right(eval(tokens.result!!))
}

fun parser(expression: String): ParseState<String, out Expression> {
    lateinit var expr: Parser<String, Token>

    val choices = org.toptobes.parsercombinator.impls.lazy {
        val operator = regex("[*+/-]")..{ Operator(it) }

        val number = digits..{ Number(it) }

        val operand = any(!expr, !number)

        operator then operand then +operand
    }..{ it.flatten() }..{ Expression(it) }

    expr = between.parentheses(choices)

    return expr(expression)
}

fun eval(token: Token): Int = when(token) {
    is Number -> {
        token.value.toInt()
    }

    is Operator -> {
        throw IllegalStateException("Operator eval'd")
    }

    is Expression -> {
        fun List<Token>.split() = Pair(first() as Operator, drop(1))

        val (operator, _operands) = token.value.split()
        val operands = _operands.map(::eval)

        when (operator.value) {
            "+" -> operands.reduce(Int::plus)
            "-" -> operands.reduce(Int::minus)
            "*" -> operands.reduce(Int::times)
            "/" -> operands.reduce(Int::div)
            else -> throw IllegalStateException("Bad operand")
        }
    }
}
