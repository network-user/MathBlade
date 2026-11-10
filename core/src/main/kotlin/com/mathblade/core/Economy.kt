package com.mathblade.core

/** Gold / XP reward pair. */
data class Reward(val gold: Int, val xp: Int)

/**
 * Economy: fixed rewards. Monster rewards come from the world table
 * ([MonsterDef.goldReward] / [MonsterDef.xpReward]).
 */
object Economy {
    val dailyReward: Reward = Reward(gold = 100, xp = 50)
    val recordChestReward: Reward = Reward(gold = 300, xp = 0)
    val errandReward: Reward = Reward(gold = 30, xp = 20)
    val contractReward: Reward = Reward(gold = 150, xp = 0)

    fun goldForMonster(monster: MonsterDef): Int = monster.goldReward
    fun xpForMonster(monster: MonsterDef): Int = monster.xpReward
}
