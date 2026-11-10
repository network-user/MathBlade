package com.mathblade.core

import kotlin.math.pow
import kotlin.random.Random

/**
 * Problem generator. Rewritten in Kotlin from the ideas of the DotMathBot
 * generator (see MathBlade-study-REPORT.md): the answer is picked first,
 * the problem is built around it; subtraction swaps so the result is >= 0;
 * division / remainder are built divisor -> quotient -> dividend;
 * roots are built from the root (radicand = root * root);
 * for powers the exponent is picked first, then a base that fits it.
 *
 * [rng] is threaded through every function, so one seed produces one
 * list of problems when the call order is the same.
 */
object ProblemGenerator {

    fun generate(difficulty: Difficulty, operation: Operation, rng: Random): MathProblem =
        when (operation) {
            Operation.ADD -> generateAdd(difficulty, rng)
            Operation.SUB -> generateSub(difficulty, rng)
            Operation.MUL -> generateMul(difficulty, rng)
            Operation.DIV -> generateDiv(difficulty, rng)
            Operation.REMAINDER -> generateRemainder(difficulty, rng)
            Operation.POWER -> generatePower(difficulty, rng)
            Operation.SQRT -> generateSqrt(difficulty, rng)
            Operation.PERCENT -> generatePercent(difficulty, rng)
            Operation.COMBO -> generateCombo(difficulty, rng)
        }

    fun generateMixed(
        difficulty: Difficulty,
        operations: List<Operation>,
        rng: Random
    ): MathProblem {
        require(operations.isNotEmpty()) { "operations must not be empty" }
        return generate(difficulty, operations[rng.nextInt(operations.size)], rng)
    }

    fun generateList(
        difficulty: Difficulty,
        operations: List<Operation>,
        count: Int,
        rng: Random
    ): List<MathProblem> {
        require(count >= 0) { "count must be >= 0" }
        return List(count) { generateMixed(difficulty, operations, rng) }
    }

    // ---- individual generators ----

    private fun addSubRange(difficulty: Difficulty): IntRange = when (difficulty) {
        Difficulty.EASY -> 2..20
        Difficulty.MEDIUM -> 20..99
        Difficulty.HARD -> 100..999
    }

    private fun generateAdd(difficulty: Difficulty, rng: Random): MathProblem {
        val range = addSubRange(difficulty)
        val a = rng.nextInt(range)
        val b = rng.nextInt(range)
        return MathProblem(Operation.ADD, listOf(a, b), a + b)
    }

    private fun generateSub(difficulty: Difficulty, rng: Random): MathProblem {
        val range = addSubRange(difficulty)
        var a = rng.nextInt(range)
        var b = rng.nextInt(range)
        if (a < b) {
            val tmp = a; a = b; b = tmp
        }
        return MathProblem(Operation.SUB, listOf(a, b), a - b)
    }

    private fun generateMul(difficulty: Difficulty, rng: Random): MathProblem {
        val (aRange, bRange) = when (difficulty) {
            Difficulty.EASY -> (2..12) to (2..12)
            Difficulty.MEDIUM -> (10..99) to (2..9)
            Difficulty.HARD -> (11..30) to (11..25)
        }
        val a = rng.nextInt(aRange)
        val b = rng.nextInt(bRange)
        return MathProblem(Operation.MUL, listOf(a, b), a * b)
    }

    private fun generateDiv(difficulty: Difficulty, rng: Random): MathProblem {
        val (divisorRange, quotientRange) = when (difficulty) {
            Difficulty.EASY -> (2..12) to (2..12)
            Difficulty.MEDIUM -> (3..12) to (8..30)
            Difficulty.HARD -> (4..20) to (12..40)
        }
        val divisor = rng.nextInt(divisorRange)
        val quotient = rng.nextInt(quotientRange)
        val dividend = divisor * quotient
        // HARD cap: dividend <= 2000 (max possible here is 20 * 40 = 800).
        require(dividend <= 2000) { "dividend exceeds 2000" }
        return MathProblem(Operation.DIV, listOf(dividend, divisor), quotient, divisor = divisor)
    }

    private fun generateRemainder(difficulty: Difficulty, rng: Random): MathProblem {
        val (divisorRange, quotientRange) = when (difficulty) {
            Difficulty.EASY -> (2..9) to (1..9)
            Difficulty.MEDIUM -> (2..15) to (1..20)
            Difficulty.HARD -> (4..25) to (1..40)
        }
        val divisor = rng.nextInt(divisorRange)
        val quotient = rng.nextInt(quotientRange)
        val remainder = rng.nextInt(1, divisor) // 1..divisor-1
        val dividend = divisor * quotient + remainder
        return MathProblem(
            Operation.REMAINDER,
            listOf(dividend, divisor),
            quotient,
            remainder = remainder,
            divisor = divisor
        )
    }

    private fun generatePower(difficulty: Difficulty, rng: Random): MathProblem {
        // Exponent first, then a base that fits it.
        val exponent: Int
        val baseRange: IntRange
        when (difficulty) {
            Difficulty.EASY -> {
                exponent = 2
                baseRange = 2..12
            }
            Difficulty.MEDIUM -> {
                exponent = if (rng.nextBoolean()) 2 else 3
                baseRange = 2..12
            }
            Difficulty.HARD -> {
                exponent = if (rng.nextBoolean()) 2 else 3
                baseRange = if (exponent == 2) 13..25 else 2..10
            }
        }
        val base = rng.nextInt(baseRange)
        val answer = base.toDouble().pow(exponent).toInt()
        return MathProblem(Operation.POWER, listOf(base), answer, exponent = exponent)
    }

    private fun generateSqrt(difficulty: Difficulty, rng: Random): MathProblem {
        val rootRange = when (difficulty) {
            Difficulty.EASY -> 2..12
            Difficulty.MEDIUM -> 2..20
            Difficulty.HARD -> 2..30
        }
        val root = rng.nextInt(rootRange)
        return MathProblem(Operation.SQRT, listOf(root * root), root)
    }

    private fun generatePercent(difficulty: Difficulty, rng: Random): MathProblem {
        val percents = when (difficulty) {
            Difficulty.EASY -> intArrayOf(10, 25, 50)
            Difficulty.MEDIUM -> intArrayOf(5, 15, 20)
            Difficulty.HARD -> intArrayOf(12, 15, 25)
        }
        val percent = percents[rng.nextInt(percents.size)]
        // Base must be a multiple of 100 / gcd(percent, 100) so the
        // answer is always an integer.
        val step = 100 / gcd(percent, 100)
        val (targetMin, targetMax) = when (difficulty) {
            Difficulty.EASY -> 100 to 900
            Difficulty.MEDIUM -> 100 to 900
            Difficulty.HARD -> 200 to 2000
        }
        val kMin = (targetMin + step - 1) / step
        val kMax = targetMax / step
        val base = step * rng.nextInt(kMin, kMax + 1)
        return MathProblem(Operation.PERCENT, listOf(base, percent), base * percent / 100)
    }

    private fun generateCombo(difficulty: Difficulty, rng: Random): MathProblem {
        // (a +/- b) * c. One (MEDIUM) or two (HARD) numbers are two-digit.
        val values = IntArray(3)
        val ranges = Array(3) { 2..9 }
        when (difficulty) {
            Difficulty.EASY -> {
                for (i in 0..2) values[i] = rng.nextInt(2, 10)
            }
            Difficulty.MEDIUM -> {
                val bigIndex = rng.nextInt(3)
                for (i in 0..2) {
                    ranges[i] = if (i == bigIndex) 10..30 else 2..9
                    values[i] = rng.nextInt(ranges[i])
                }
            }
            Difficulty.HARD -> {
                val smallIndex = rng.nextInt(3)
                for (i in 0..2) {
                    ranges[i] = if (i == smallIndex) 2..9 else 10..30
                    values[i] = rng.nextInt(ranges[i])
                }
            }
        }
        var a = values[0]
        var b = values[1]
        val c = values[2]
        val subtract = rng.nextBoolean()
        if (subtract) {
            if (a < b) {
                val tmp = a; a = b; b = tmp
            }
            // Avoid a zero intermediate result: re-roll b in the range
            // it was drawn from until a > b.
            while (a == b) {
                b = rng.nextInt(ranges[1])
                if (b > a) {
                    val tmp = a; a = b; b = tmp
                }
            }
        }
        val inner = if (subtract) a - b else a + b
        val answer = inner * c
        // Caps from the spec: intermediate and final result <= 2000.
        // By construction the worst case is (30 + 30) * 30 = 1800.
        require(inner in 0..2000 && answer in 0..2000) { "combo result exceeds 2000" }
        return MathProblem(
            Operation.COMBO,
            listOf(a, b, c),
            answer,
            innerSubtract = subtract
        )
    }

    // ---- helpers ----

    /** Inclusive random int from a range. */
    private fun Random.nextInt(range: IntRange): Int =
        nextInt(range.first, range.last + 1)

    private fun gcd(a: Int, b: Int): Int {
        var x = a
        var y = b
        while (y != 0) {
            val t = x % y
            x = y
            y = t
        }
        return x
    }
}
