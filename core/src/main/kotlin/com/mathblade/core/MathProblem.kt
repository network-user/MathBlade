package com.mathblade.core

/**
 * One math problem. The problem carries only numbers and identifiers;
 * :app renders the text from [operation] and [operands].
 *
 * Operands layout per operation:
 * - ADD / SUB / MUL: [a, b]
 * - DIV: [dividend, divisor]; [divisor] is also stored separately
 * - REMAINDER: [dividend, divisor]; [answer] is the quotient,
 *   [remainder] is the remainder; [divisor] is stored separately
 * - POWER: [base]; [exponent] is the exponent
 * - SQRT: [radicand]; [answer] is the root
 * - PERCENT: [base, percent]; [answer] is base * percent / 100
 * - COMBO: [a, b, c], answer is (a +/- b) * c; [innerSubtract] is true
 *   when the inner operation is subtraction, false for addition
 */
data class MathProblem(
    val operation: Operation,
    val operands: List<Int>,
    val answer: Int,
    val remainder: Int? = null,
    val exponent: Int? = null,
    val divisor: Int? = null,
    val innerSubtract: Boolean = false
)

/**
 * Parses a typed answer. Returns null for a null / blank / non-numeric
 * string; null is "not an attempt" and must not consume a life or a turn.
 */
fun parseAnswer(raw: String?): Int? {
    if (raw == null) return null
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    return trimmed.toIntOrNull()
}

/** Strict integer equality check for a single-number answer. */
fun checkAnswer(problem: MathProblem, answer: Int): Boolean =
    problem.operation != Operation.REMAINDER && problem.answer == answer

/**
 * Remainder check: both the quotient and the remainder must match.
 * A correct quotient with a wrong remainder (or vice versa) is wrong.
 */
fun checkRemainderAnswer(problem: MathProblem, quotient: Int, remainder: Int): Boolean =
    problem.operation == Operation.REMAINDER &&
        problem.answer == quotient &&
        problem.remainder == remainder
