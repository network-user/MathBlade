package com.mathblade.core

/**
 * Equipment: swords, armor, amulets, forge.
 * All ids are language-neutral; display text lives in :app strings.
 */
data class SwordDef(
    val id: String,
    val multiplier: Double,
    val speedCapBonus: Double,
    val typeBonusPercent: Map<Operation, Double>,
    val preciseBonus: Int,
    val regionObtained: Int
) {
    /** Forge level adds +0.05 per level on top of the base multiplier. */
    fun effectiveMultiplier(level: Int): Double = multiplier + 0.05 * level

    /** Effective speed cap: base cap plus this sword's bonus. */
    fun effectiveSpeedCap(base: Double = 1.6): Double = base + speedCapBonus

    /** School bonus as a damage multiplier (1.0 when no bonus applies). */
    fun typeBonusFor(operation: Operation): Double =
        1.0 + (typeBonusPercent[operation] ?: 0.0)
}

data class ArmorDef(
    val id: String,
    val maxLives: Int,
    val price: Int,
    val requiredRegionCleared: Int
)

data class AmuletDef(
    val id: String,
    val price: Int,
    val requiredRegionCleared: Int
)

object Equipment {

    val swords: List<SwordDef> = listOf(
        SwordDef("rusty", 1.00, 0.0, emptyMap(), 0, 1),
        SwordDef("apprentice", 1.15, 0.1, emptyMap(), 0, 1),
        SwordDef("caravan", 1.30, 0.0, mapOf(
            Operation.MUL to 0.15,
            Operation.DIV to 0.15,
            Operation.PERCENT to 0.15
        ), 0, 2),
        SwordDef("remainder", 1.25, 0.0, mapOf(
            Operation.REMAINDER to 0.20
        ), 0, 3),
        SwordDef("archmage", 1.50, 0.2, emptyMap(), 8, 5)
    )

    val armors: List<ArmorDef> = listOf(
        ArmorDef("canvas", 3, 0, 0),
        ArmorDef("leather", 4, 200, 1),
        ArmorDef("chainmail", 5, 500, 3)
    )

    val amulets: List<AmuletDef> = listOf(
        AmuletDef("beads", 150, 1),
        AmuletDef("hourglass", 350, 2)
    )

    fun swordById(id: String): SwordDef? = swords.find { it.id == id }
    fun armorById(id: String): ArmorDef? = armors.find { it.id == id }
    fun amuletById(id: String): AmuletDef? = amulets.find { it.id == id }

    /** "beads": shows the last digit of the answer once per battle. */
    fun amuletRevealsLastDigit(amuletId: String?): Boolean = amuletId == "beads"

    /** "hourglass": time norm x1.25 (slower speed decay). */
    fun amuletTimeNormMultiplier(amuletId: String?): Double =
        if (amuletId == "hourglass") 1.25 else 1.0
}

enum class ForgeResult {
    IN_PROGRESS,
    SUCCESS,
    FAILED
}

data class ForgeAttempt(
    val swordId: String,
    val correctCount: Int,
    val wrongCount: Int,
    val targetLevel: Int
)

data class ForgeSuccessResult(
    val newGold: Int,
    val newForgeLevels: Map<String, Int>
)

object Forge {
    const val NEED_CORRECT: Int = 5
    const val MAX_WRONG: Int = 3
    const val MAX_LEVEL: Int = 5

    /** Price to forge from [level] to level+1: 50 * (level+1) * regionObtained. */
    fun forgePrice(sword: SwordDef, level: Int): Int =
        50 * (level + 1) * sword.regionObtained

    /** Starts a forge attempt, or null when the sword is already max level. */
    fun startForge(swordId: String, currentLevel: Int): ForgeAttempt? {
        if (currentLevel >= MAX_LEVEL) return null
        return ForgeAttempt(swordId, 0, 0, currentLevel + 1)
    }

    /** Records one forge answer; returns the updated attempt. */
    fun recordForgeAnswer(attempt: ForgeAttempt, correct: Boolean): ForgeAttempt =
        if (correct) {
            attempt.copy(correctCount = attempt.correctCount + 1)
        } else {
            attempt.copy(wrongCount = attempt.wrongCount + 1)
        }

    /** Current result of an attempt. */
    fun resultOf(attempt: ForgeAttempt): ForgeResult = when {
        attempt.correctCount >= NEED_CORRECT -> ForgeResult.SUCCESS
        attempt.wrongCount >= MAX_WRONG -> ForgeResult.FAILED
        else -> ForgeResult.IN_PROGRESS
    }

    fun canAfford(gold: Int, price: Int): Boolean = gold >= price

    /**
     * Applies a successful forge: gold is deducted only here, and only
     * when the caller already knows the result is SUCCESS. Returns null
     * when the player cannot afford it, the target level is out of range,
     * or the level would go backwards.
     */
    fun applyForgeSuccess(
        gold: Int,
        sword: SwordDef,
        targetLevel: Int,
        forgeLevels: Map<String, Int>
    ): ForgeSuccessResult? {
        val currentLevel = forgeLevels[sword.id] ?: 0
        if (targetLevel <= currentLevel || targetLevel > MAX_LEVEL) return null
        val price = forgePrice(sword, currentLevel)
        if (!canAfford(gold, price)) return null
        val newLevels = forgeLevels + (sword.id to targetLevel)
        return ForgeSuccessResult(gold - price, newLevels)
    }
}
