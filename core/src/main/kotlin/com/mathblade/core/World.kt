package com.mathblade.core

/**
 * World table: regions, monsters and bosses. Single source of truth for
 * :core and :app UI. [RegionDef.nameId] and monster ids are language
 * neutral identifiers; display names live in :app strings.
 *
 * HP rule of thumb: 4-6 hits per regular monster, 10-12 per boss at the
 * average damage of the region (base damage * typical speed ~1.0-1.3,
 * sword multiplier and precise bonus included).
 */
data class MonsterDef(
    val id: String,
    val regionId: Int,
    val operation: Operation,
    val hp: Int,
    val goldReward: Int,
    val xpReward: Int,
    val isBoss: Boolean,
    val phases: Int = 1
)

data class RegionDef(
    val id: Int,
    val nameId: String,
    val monsters: List<MonsterDef>,
    val boss: MonsterDef,
    val difficulty: Difficulty
)

/** One boss phase: the school and difficulty the boss switches to. */
data class BossPhase(
    val operation: Operation,
    val difficulty: Difficulty,
    /** When set, the phase draws mixed problems from these operations. */
    val mixedOperations: List<Operation>? = null
)

object World {

    val regions: List<RegionDef> = listOf(
        RegionDef(
            id = 1,
            nameId = "region_outskirts",
            monsters = listOf(
                MonsterDef("slime_add", 1, Operation.ADD, 60, 25, 25, false),
                MonsterDef("wolf_sub", 1, Operation.SUB, 60, 25, 25, false)
            ),
            boss = MonsterDef("ancient_slime", 1, Operation.ADD, 340, 100, 100, true, phases = 3),
            difficulty = Difficulty.EASY
        ),
        RegionDef(
            id = 2,
            nameId = "region_trade_route",
            monsters = listOf(
                MonsterDef("bat_mul", 2, Operation.MUL, 100, 40, 40, false),
                MonsterDef("tax_div", 2, Operation.DIV, 100, 40, 40, false),
                MonsterDef("usurer_pct", 2, Operation.PERCENT, 100, 40, 40, false)
            ),
            boss = MonsterDef("caravan_master", 2, Operation.MUL, 360, 140, 140, true, phases = 3),
            difficulty = Difficulty.MEDIUM
        ),
        RegionDef(
            id = 3,
            nameId = "region_remainder_tower",
            monsters = listOf(
                MonsterDef("golem_rem1", 3, Operation.REMAINDER, 140, 55, 55, false),
                MonsterDef("golem_rem2", 3, Operation.REMAINDER, 140, 55, 55, false)
            ),
            boss = MonsterDef("remainder_keeper", 3, Operation.REMAINDER, 380, 160, 160, true, phases = 3),
            difficulty = Difficulty.MEDIUM
        ),
        RegionDef(
            id = 4,
            nameId = "region_power_ruins",
            monsters = listOf(
                MonsterDef("griffin_pow", 4, Operation.POWER, 160, 70, 70, false),
                MonsterDef("root_beast", 4, Operation.SQRT, 160, 70, 70, false),
                MonsterDef("scribe_combo", 4, Operation.COMBO, 160, 70, 70, false)
            ),
            boss = MonsterDef("chronicler", 4, Operation.POWER, 400, 180, 180, true, phases = 3),
            difficulty = Difficulty.HARD
        ),
        RegionDef(
            id = 5,
            nameId = "region_throne_hall",
            monsters = emptyList(),
            boss = MonsterDef("archchancellor", 5, Operation.COMBO, 420, 250, 250, true, phases = 3),
            difficulty = Difficulty.HARD
        )
    )

    fun regionById(id: Int): RegionDef? = regions.find { it.id == id }

    fun monsterById(id: String): MonsterDef? =
        regions.flatMap { it.monsters + it.boss }.find { it.id == id }

    /**
     * Boss phase tables. Phase 1 is the boss's own school/difficulty.
     * Region 3 boss uses HARD on phase 3. The region 5 boss draws mixed
     * problems from all schools in every phase ("all schools").
     */
    fun bossPhases(bossId: String): List<BossPhase> {
        val all = Operation.entries.toList()
        return when (bossId) {
            "ancient_slime" -> listOf(
                BossPhase(Operation.ADD, Difficulty.EASY),
                BossPhase(Operation.ADD, Difficulty.EASY),
                BossPhase(Operation.SUB, Difficulty.MEDIUM)
            )
            "caravan_master" -> listOf(
                BossPhase(Operation.MUL, Difficulty.MEDIUM),
                BossPhase(Operation.DIV, Difficulty.MEDIUM),
                BossPhase(Operation.PERCENT, Difficulty.MEDIUM)
            )
            "remainder_keeper" -> listOf(
                BossPhase(Operation.REMAINDER, Difficulty.MEDIUM),
                BossPhase(Operation.REMAINDER, Difficulty.MEDIUM),
                BossPhase(Operation.REMAINDER, Difficulty.HARD)
            )
            "chronicler" -> listOf(
                BossPhase(Operation.POWER, Difficulty.HARD),
                BossPhase(Operation.SQRT, Difficulty.HARD),
                BossPhase(Operation.COMBO, Difficulty.HARD)
            )
            "archchancellor" -> listOf(
                BossPhase(Operation.COMBO, Difficulty.HARD, mixedOperations = all),
                BossPhase(Operation.POWER, Difficulty.HARD, mixedOperations = all),
                BossPhase(Operation.PERCENT, Difficulty.HARD, mixedOperations = all)
            )
            else -> {
                val boss = monsterById(bossId)
                if (boss != null) {
                    val region = regionById(boss.regionId)
                    listOf(BossPhase(boss.operation, region?.difficulty ?: Difficulty.MEDIUM))
                } else {
                    emptyList()
                }
            }
        }
    }
}

/**
 * Boss battle with phases. Each phase threshold (hp <= 60% and <= 30% of
 * max) fires once: the boss changes school/difficulty, the hero regains
 * at most 1 life for that phase (only if lives < maxLives), the animation
 * becomes PHASE_CHANGE, and damage already dealt is never rolled back.
 */
class BossBattle(
    val boss: MonsterDef,
    val maxLives: Int,
    initialLives: Int = maxLives,
    val phases: List<BossPhase> = World.bossPhases(boss.id),
    val swordMultiplier: Double = 1.0,
    val speedCap: Double = 1.6,
    val preciseBonus: Int = 0
) {
    val maxHp: Int = boss.hp
    var hp: Int = boss.hp
        private set
    var lives: Int = initialLives.coerceIn(0, maxLives)
        private set
    var phase: Int = 1
        private set
    var currentOperation: Operation = phases.firstOrNull()?.operation ?: boss.operation
        private set
    var currentDifficulty: Difficulty = phases.firstOrNull()?.difficulty ?: Difficulty.MEDIUM
        private set
    var currentAnimation: CombatAnimationState = CombatAnimationState.IDLE
        private set

    private var phase2Triggered: Boolean = false
    private var phase3Triggered: Boolean = false

    val isRunOver: Boolean get() = lives <= 0
    val isEnemyDead: Boolean get() = hp <= 0

    /** Operations the current phase draws problems from. */
    fun operationsForCurrentPhase(): List<Operation> {
        val phaseDef = phases.getOrNull(phase - 1)
        return phaseDef?.mixedOperations ?: listOf(currentOperation)
    }

    fun answerCorrect(problem: MathProblem, elapsedMs: Long, typeBonus: Double = 1.0): DamageBreakdown {
        val breakdown = computeDamage(
            currentDifficulty, elapsedMs, swordMultiplier, speedCap, typeBonus, preciseBonus
        )
        hp = (hp - breakdown.total).coerceAtLeast(0)
        if (isEnemyDead) {
            currentAnimation = CombatAnimationState.ENEMY_DYING
        } else {
            currentAnimation = CombatAnimationState.HERO_ATTACK
            checkPhaseTriggers()
        }
        return breakdown
    }

    fun answerWrong() {
        lives = (lives - 1).coerceAtLeast(0)
        currentAnimation = CombatAnimationState.HERO_HURT
    }

    /**
     * Checks phase thresholds and applies each untriggered one.
     * Returns true if at least one phase fired on this call.
     */
    fun checkPhaseTriggers(): Boolean {
        var fired = false
        if (!phase2Triggered && phases.size >= 2 && hp <= maxHp * 0.6) {
            applyPhase(2)
            fired = true
        }
        if (!phase3Triggered && phases.size >= 3 && hp <= maxHp * 0.3) {
            applyPhase(3)
            fired = true
        }
        return fired
    }

    private fun applyPhase(newPhase: Int) {
        val phaseDef = phases.getOrNull(newPhase - 1) ?: return
        if (newPhase == 2) phase2Triggered = true
        if (newPhase == 3) phase3Triggered = true
        phase = newPhase
        currentOperation = phaseDef.operation
        currentDifficulty = phaseDef.difficulty
        if (lives < maxLives) {
            lives += 1
        }
        currentAnimation = CombatAnimationState.PHASE_CHANGE
    }
}
