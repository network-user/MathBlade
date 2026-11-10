package com.mathblade.core

/**
 * Difficulty of a math problem / battle.
 *
 * baseDamage: base damage dealt for a correct answer before multipliers.
 * normSeconds: time (seconds) considered a "normal" answer; answering in
 * exactly this time gives speed multiplier 1.0.
 */
enum class Difficulty(val baseDamage: Int, val normSeconds: Int) {
    EASY(12, 8),
    MEDIUM(20, 15),
    HARD(32, 25)
}
