package com.mathblade.core

import kotlin.math.roundToInt

/**
 * Damage formula and battle state. Pure numbers only: [DamageBreakdown]
 * text contains digits, "x" and "=" and nothing else language-specific.
 *
 * speed = normSeconds * 1000 / elapsedMs, clamped to [0.6, speedCap].
 * damage = round(base * speed * swordMultiplier * typeBonus), at least 1.
 * If speed > 1.3, [preciseBonus] is added after rounding.
 */
data class DamageBreakdown(
    val base: Int,
    val speedMultiplier: Double,
    val swordMultiplier: Double,
    val typeBonus: Double,
    val preciseBonus: Int,
    val total: Int,
    val breakdownText: String
)

fun computeDamage(
    difficulty: Difficulty,
    elapsedMs: Long,
    swordMultiplier: Double = 1.0,
    speedCap: Double = 1.6,
    typeBonus: Double = 1.0,
    preciseBonus: Int = 0
): DamageBreakdown {
    val base = difficulty.baseDamage
    val rawSpeed = if (elapsedMs <= 0L) {
        speedCap
    } else {
        difficulty.normSeconds * 1000.0 / elapsedMs
    }
    val speed = rawSpeed.coerceIn(0.6, speedCap)
    var total = (base * speed * swordMultiplier * typeBonus).roundToInt().coerceAtLeast(1)
    val preciseApplied = if (speed > 1.3) preciseBonus else 0
    total += preciseApplied
    val text = buildString {
        append(base)
        append(" x ")
        append(format2(speed))
        append(" x ")
        append(format2(swordMultiplier))
        if (typeBonus != 1.0) {
            append(" x ")
            append(format2(typeBonus))
        }
        if (preciseApplied != 0) {
            append(" + ")
            append(preciseApplied)
        }
        append(" = ")
        append(total)
    }
    return DamageBreakdown(base, speed, swordMultiplier, typeBonus, preciseApplied, total, text)
}

/** Rounded to two decimals; a trailing ".0" is dropped ("1.0" -> "1"). */
internal fun format2(value: Double): String {
    val rounded = (value * 100).roundToInt() / 100.0
    val text = rounded.toString()
    return if (text.endsWith(".0")) text.dropLast(2) else text
}

enum class CombatAnimationState {
    IDLE,
    HERO_ATTACK,
    HERO_HURT,
    PHASE_CHANGE,
    ENEMY_DYING,
    VICTORY
}

/**
 * One battle against a regular monster: hero lives vs monster HP.
 * Damage parameters are fixed for the battle; [answerCorrect] takes the
 * per-problem type bonus (school bonus of the equipped sword).
 */
class BattleState(
    val maxLives: Int,
    monsterHp: Int,
    val difficulty: Difficulty,
    val swordMultiplier: Double = 1.0,
    val speedCap: Double = 1.6,
    val preciseBonus: Int = 0,
    initialLives: Int = maxLives
) {
    var lives: Int = initialLives.coerceIn(0, maxLives)
        private set
    var monsterHp: Int = monsterHp
        private set
    val monsterMaxHp: Int = monsterHp
    var currentAnimation: CombatAnimationState = CombatAnimationState.IDLE
        private set

    val isRunOver: Boolean get() = lives <= 0
    val isEnemyDead: Boolean get() = monsterHp <= 0

    fun answerCorrect(
        problem: MathProblem,
        elapsedMs: Long,
        typeBonus: Double = 1.0,
        difficulty: Difficulty = this.difficulty
    ): DamageBreakdown {
        val breakdown = computeDamage(
            difficulty, elapsedMs, swordMultiplier, speedCap, typeBonus, preciseBonus
        )
        monsterHp = (monsterHp - breakdown.total).coerceAtLeast(0)
        currentAnimation = if (isEnemyDead) {
            CombatAnimationState.ENEMY_DYING
        } else {
            CombatAnimationState.HERO_ATTACK
        }
        return breakdown
    }

    fun answerWrong() {
        lives = (lives - 1).coerceAtLeast(0)
        currentAnimation = CombatAnimationState.HERO_HURT
    }
}
