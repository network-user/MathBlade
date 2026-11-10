package com.mathblade.core

/**
 * Training battle difficulty unlocks by experience:
 * xp >= 0 EASY, xp >= 500 MEDIUM, xp >= 1500 HARD.
 */
object Training {
    fun unlockedDifficulty(xp: Int): Difficulty = when {
        xp >= 1500 -> Difficulty.HARD
        xp >= 500 -> Difficulty.MEDIUM
        else -> Difficulty.EASY
    }
}
