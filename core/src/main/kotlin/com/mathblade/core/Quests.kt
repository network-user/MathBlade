package com.mathblade.core

/**
 * Quests, contracts and titles. All ids are language-neutral;
 * display text lives in :app strings.
 */
data class QuestDef(
    val id: String,
    val isMain: Boolean
)

object Quests {
    val mainQuests: List<QuestDef> = listOf(
        QuestDef("main_region_1", true),
        QuestDef("main_region_2", true),
        QuestDef("main_region_3", true),
        QuestDef("main_region_4", true),
        QuestDef("main_region_5", true)
    )

    val sideQuests: List<QuestDef> = listOf(
        QuestDef("forge_level1", false),
        QuestDef("herald_daily", false),
        QuestDef("treasurer_500", false),
        QuestDef("mentor_region2_flawless", false)
    )

    val all: List<QuestDef> = mainQuests + sideQuests
}

/**
 * Tracks one-time completions. [complete] returns true only the first
 * time an id is completed; a repeated completion returns false and
 * changes nothing.
 */
class QuestProgress(
    val completedOnce: MutableSet<String> = mutableSetOf()
) {
    fun complete(id: String): Boolean = completedOnce.add(id)

    fun isCompleted(id: String): Boolean = id in completedOnce
}

data class ContractDef(val id: String)

object Contracts {
    val streak15: ContractDef = ContractDef("streak15")
    val fast10easy: ContractDef = ContractDef("fast10easy")
    val duelBoss: ContractDef = ContractDef("duel_boss")

    val all: List<ContractDef> = listOf(streak15, fast10easy, duelBoss)

    /** 15 correct answers in a row inside one session. */
    fun isStreak15(streak: Int): Boolean = streak >= 15

    /**
     * 10 easy problems in under 90 seconds total. [elapsedMs] holds one
     * entry per solved problem, in order.
     */
    fun isFast10Easy(elapsedMs: List<Long>): Boolean =
        elapsedMs.size >= 10 && elapsedMs.take(10).sum() < 90_000L

    /** Boss duel won starting with exactly 1 life. */
    fun isDuelBossWon(livesAtStart: Int, won: Boolean): Boolean =
        livesAtStart == 1 && won
}

object Titles {
    const val APPRENTICE: String = "title_apprentice" // 0
    const val SQUIRE: String = "title_squire" // 1000
    const val KNIGHT: String = "title_knight" // 5000
    const val MASTER: String = "title_master" // 15000
    const val ARCHMAGE: String = "title_archmage" // 30000

    /** Title id for a given total damage dealt (sum over all battles). */
    fun titleFor(totalDamage: Long): String = when {
        totalDamage >= 30_000L -> ARCHMAGE
        totalDamage >= 15_000L -> MASTER
        totalDamage >= 5_000L -> KNIGHT
        totalDamage >= 1_000L -> SQUIRE
        else -> APPRENTICE
    }
}
