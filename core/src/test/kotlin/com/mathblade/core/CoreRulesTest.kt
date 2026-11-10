package com.mathblade.core

import java.time.LocalDate
import kotlin.math.pow
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreRulesTest {

    // ---- generator: seed stability ----

    @Test
    fun `same seed gives same problem list`() {
        val ops = Operation.entries.toList()
        val first = ProblemGenerator.generateList(Difficulty.MEDIUM, ops, 20, Random(12345))
        val second = ProblemGenerator.generateList(Difficulty.MEDIUM, ops, 20, Random(12345))
        assertEquals(first, second)
        val other = ProblemGenerator.generateList(Difficulty.MEDIUM, ops, 20, Random(999))
        assertNotEquals(first, other)
    }

    // ---- generator: ranges and integer answers, all 9 ops x 3 difficulties ----

    @Test
    fun `generator ranges hold for every operation and difficulty`() {
        for (difficulty in Difficulty.entries) {
            for (operation in Operation.entries) {
                repeat(200) { i ->
                    val rng = Random(1000L * (difficulty.ordinal + 1) + 77L * operation.ordinal + i)
                    val p = ProblemGenerator.generate(difficulty, operation, rng)
                    assertEquals(operation, p.operation)
                    checkProblemRanges(difficulty, p)
                }
            }
        }
    }

    private fun checkProblemRanges(difficulty: Difficulty, p: MathProblem) {
        when (p.operation) {
            Operation.ADD -> {
                val range = when (difficulty) {
                    Difficulty.EASY -> 2..20
                    Difficulty.MEDIUM -> 20..99
                    Difficulty.HARD -> 100..999
                }
                assertTrue(p.operands[0] in range)
                assertTrue(p.operands[1] in range)
                assertEquals(p.operands[0] + p.operands[1], p.answer)
            }
            Operation.SUB -> {
                val range = when (difficulty) {
                    Difficulty.EASY -> 2..20
                    Difficulty.MEDIUM -> 20..99
                    Difficulty.HARD -> 100..999
                }
                assertTrue(p.operands[0] in range)
                assertTrue(p.operands[1] in range)
                assertTrue("subtraction result must be >= 0", p.answer >= 0)
                assertEquals(p.operands[0] - p.operands[1], p.answer)
            }
            Operation.MUL -> {
                val (aRange, bRange) = when (difficulty) {
                    Difficulty.EASY -> (2..12) to (2..12)
                    Difficulty.MEDIUM -> (10..99) to (2..9)
                    Difficulty.HARD -> (11..30) to (11..25)
                }
                assertTrue(p.operands[0] in aRange)
                assertTrue(p.operands[1] in bRange)
                assertEquals(p.operands[0] * p.operands[1], p.answer)
            }
            Operation.DIV -> {
                val (divisorRange, quotientRange) = when (difficulty) {
                    Difficulty.EASY -> (2..12) to (2..12)
                    Difficulty.MEDIUM -> (3..12) to (8..30)
                    Difficulty.HARD -> (4..20) to (12..40)
                }
                val dividend = p.operands[0]
                val divisor = p.operands[1]
                assertTrue(divisor in divisorRange)
                assertTrue(p.answer in quotientRange)
                assertEquals(divisor, p.divisor)
                assertTrue("division must be exact", dividend % divisor == 0)
                assertEquals(dividend / divisor, p.answer)
                assertTrue("dividend must be <= 2000", dividend <= 2000)
            }
            Operation.REMAINDER -> {
                val (divisorRange, quotientRange) = when (difficulty) {
                    Difficulty.EASY -> (2..9) to (1..9)
                    Difficulty.MEDIUM -> (2..15) to (1..20)
                    Difficulty.HARD -> (4..25) to (1..40)
                }
                val dividend = p.operands[0]
                val divisor = p.operands[1]
                assertTrue(divisor in divisorRange)
                assertTrue(p.answer in quotientRange)
                assertEquals(divisor, p.divisor)
                val remainder = p.remainder!!
                assertTrue("remainder must be 1..divisor-1", remainder in 1 until divisor)
                assertEquals(dividend, divisor * p.answer + remainder)
            }
            Operation.POWER -> {
                val base = p.operands[0]
                val exp = p.exponent!!
                assertEquals(base.toDouble().pow(exp).toInt(), p.answer)
                when (difficulty) {
                    Difficulty.EASY -> {
                        assertEquals(2, exp)
                        assertTrue(base in 2..12)
                    }
                    Difficulty.MEDIUM -> {
                        assertTrue(exp == 2 || exp == 3)
                        assertTrue(base in 2..12)
                    }
                    Difficulty.HARD -> {
                        if (exp == 2) assertTrue(base in 13..25) else assertEquals(3, exp)
                        if (exp == 3) assertTrue(base in 2..10)
                    }
                }
            }
            Operation.SQRT -> {
                val rootRange = when (difficulty) {
                    Difficulty.EASY -> 2..12
                    Difficulty.MEDIUM -> 2..20
                    Difficulty.HARD -> 2..30
                }
                assertTrue(p.answer in rootRange)
                assertEquals(p.answer * p.answer, p.operands[0])
            }
            Operation.PERCENT -> {
                val percents = when (difficulty) {
                    Difficulty.EASY -> setOf(10, 25, 50)
                    Difficulty.MEDIUM -> setOf(5, 15, 20)
                    Difficulty.HARD -> setOf(12, 15, 25)
                }
                val base = p.operands[0]
                val percent = p.operands[1]
                assertTrue(percent in percents)
                assertTrue("percent result must be exact", (base * percent) % 100 == 0)
                assertEquals(base * percent / 100, p.answer)
            }
            Operation.COMBO -> {
                val a = p.operands[0]
                val b = p.operands[1]
                val c = p.operands[2]
                val inner = if (p.innerSubtract) a - b else a + b
                assertTrue("combo inner result must be >= 0", inner >= 0)
                assertTrue("combo inner result must be <= 2000", inner <= 2000)
                assertTrue("combo final result must be <= 2000", p.answer <= 2000)
                assertEquals(inner * c, p.answer)
                when (difficulty) {
                    Difficulty.EASY -> {
                        assertTrue(a in 2..9 && b in 2..9 && c in 2..9)
                    }
                    Difficulty.MEDIUM -> {
                        val big = listOf(a, b, c).count { it in 10..30 }
                        assertTrue("medium combo needs one 10..30 number", big >= 1)
                    }
                    Difficulty.HARD -> {
                        val big = listOf(a, b, c).count { it in 10..30 }
                        assertTrue("hard combo needs two 10..30 numbers", big >= 2)
                    }
                }
            }
        }
    }

    // ---- answer parsing and remainder check ----

    @Test
    fun `parseAnswer returns null for blank and garbage`() {
        assertNull(parseAnswer(null))
        assertNull(parseAnswer(""))
        assertNull(parseAnswer("   "))
        assertNull(parseAnswer("abc"))
        assertNull(parseAnswer("12x"))
        assertEquals(42, parseAnswer("42"))
        assertEquals(7, parseAnswer("  7 "))
        assertEquals(-3, parseAnswer("-3"))
    }

    @Test
    fun `remainder answer needs both quotient and remainder`() {
        val rng = Random(42)
        val p = ProblemGenerator.generate(Difficulty.EASY, Operation.REMAINDER, rng)
        assertTrue(checkRemainderAnswer(p, p.answer, p.remainder!!))
        assertFalse(checkRemainderAnswer(p, p.answer, p.remainder!! + 1))
        assertFalse(checkRemainderAnswer(p, p.answer + 1, p.remainder!!))
        assertFalse(checkRemainderAnswer(p, p.answer + 1, p.remainder!! + 1))
    }

    // ---- damage formula ----

    @Test
    fun `damage clamps speed at 0_6 and 1_6 caps`() {
        // EASY norm is 8 s. 4000 ms -> raw speed 2.0, capped to 1.6.
        assertEquals(19, computeDamage(Difficulty.EASY, 4000L).total)
        // 20000 ms -> raw speed 0.4, floored to 0.6.
        assertEquals(7, computeDamage(Difficulty.EASY, 20000L).total)
        // 8000 ms -> speed exactly 1.0.
        assertEquals(12, computeDamage(Difficulty.EASY, 8000L).total)
        // MEDIUM norm 15 s, 15000 ms -> speed 1.0.
        assertEquals(20, computeDamage(Difficulty.MEDIUM, 15000L).total)
        // HARD norm 25 s, 25000 ms -> speed 1.0.
        assertEquals(32, computeDamage(Difficulty.HARD, 25000L).total)
    }

    @Test
    fun `precise bonus applies only when speed above 1_3`() {
        // EASY 5000 ms -> speed capped 1.6 > 1.3: 19 + 8 = 27.
        val fast = computeDamage(Difficulty.EASY, 5000L, preciseBonus = 8)
        assertEquals(27, fast.total)
        assertEquals(8, fast.preciseBonus)
        // EASY 8000 ms -> speed 1.0: no precise bonus.
        val normal = computeDamage(Difficulty.EASY, 8000L, preciseBonus = 8)
        assertEquals(12, normal.total)
        assertEquals(0, normal.preciseBonus)
    }

    @Test
    fun `sword forge school bonus and precise strike stack in damage`() {
        val archmage = Equipment.swordById("archmage")!!
        assertEquals(1.6, archmage.effectiveMultiplier(2), 1e-9)
        assertEquals(1.8, archmage.effectiveSpeedCap(), 1e-9)
        // MEDIUM, speed 1.0, sword 1.6, school 1.15: round(20 * 1.6 * 1.15) = 37.
        val hit = computeDamage(
            Difficulty.MEDIUM, 15000L,
            swordMultiplier = archmage.effectiveMultiplier(2),
            speedCap = archmage.effectiveSpeedCap(),
            typeBonus = 1.15,
            preciseBonus = archmage.preciseBonus
        )
        assertEquals(37, hit.total)
        // Same, but fast (5000 ms -> speed capped 1.8): round(20*1.8*1.6*1.15)=66, +8 = 74.
        val fastHit = computeDamage(
            Difficulty.MEDIUM, 5000L,
            swordMultiplier = archmage.effectiveMultiplier(2),
            speedCap = archmage.effectiveSpeedCap(),
            typeBonus = 1.15,
            preciseBonus = archmage.preciseBonus
        )
        assertEquals(74, fastHit.total)
        // Caravan sword school bonus: +15% vs MUL.
        val caravan = Equipment.swordById("caravan")!!
        assertEquals(1.15, caravan.typeBonusFor(Operation.MUL), 1e-9)
        assertEquals(1.0, caravan.typeBonusFor(Operation.ADD), 1e-9)
    }

    @Test
    fun `damage breakdown text has numbers only`() {
        val breakdown = computeDamage(Difficulty.EASY, 8000L)
        assertEquals("12 x 1 x 1 = 12", breakdown.breakdownText)
    }

    // ---- battle state ----

    @Test
    fun `wrong answer removes a life and leaves monster hp unchanged`() {
        val battle = BattleState(maxLives = 3, monsterHp = 60, difficulty = Difficulty.EASY)
        battle.answerWrong()
        assertEquals(2, battle.lives)
        assertEquals(60, battle.monsterHp)
        assertEquals(CombatAnimationState.HERO_HURT, battle.currentAnimation)
        assertFalse(battle.isRunOver)
    }

    @Test
    fun `zero lives ends the run`() {
        val battle = BattleState(maxLives = 2, monsterHp = 60, difficulty = Difficulty.EASY)
        battle.answerWrong()
        battle.answerWrong()
        assertEquals(0, battle.lives)
        assertTrue(battle.isRunOver)
        battle.answerWrong()
        assertEquals(0, battle.lives)
    }

    @Test
    fun `correct answer damages monster and killing blow ends the fight`() {
        val battle = BattleState(maxLives = 3, monsterHp = 12, difficulty = Difficulty.EASY)
        val problem = ProblemGenerator.generate(Difficulty.EASY, Operation.ADD, Random(7))
        val breakdown = battle.answerCorrect(problem, 8000L)
        assertEquals(12, breakdown.total)
        assertEquals(0, battle.monsterHp)
        assertTrue(battle.isEnemyDead)
        assertEquals(CombatAnimationState.ENEMY_DYING, battle.currentAnimation)
    }

    // ---- boss phases ----

    @Test
    fun `boss phases fire once and grant at most one life each`() {
        val boss = World.monsterById("ancient_slime")!!
        val battle = BossBattle(boss = boss, maxLives = 3, initialLives = 2)
        val problem = ProblemGenerator.generate(Difficulty.EASY, Operation.ADD, Random(7))

        // Damage down to <= 60% of 340 (<= 204): phase 2 fires once.
        while (battle.hp > 204) {
            battle.answerCorrect(problem, 1000L)
        }
        assertEquals(2, battle.phase)
        assertEquals(3, battle.lives) // +1 life, capped at max
        assertEquals(Operation.ADD, battle.currentOperation)
        assertEquals(Difficulty.EASY, battle.currentDifficulty)

        // Re-checking changes nothing: phase 2 already fired.
        val livesAfterPhase2 = battle.lives
        assertFalse(battle.checkPhaseTriggers())
        assertEquals(livesAfterPhase2, battle.lives)

        // Damage down to <= 30% of 340 (<= 102): phase 3 fires once.
        while (battle.hp > 102) {
            battle.answerCorrect(problem, 1000L)
        }
        assertEquals(3, battle.phase)
        assertEquals(Operation.SUB, battle.currentOperation)
        assertEquals(Difficulty.MEDIUM, battle.currentDifficulty)
        assertEquals(3, battle.lives) // already at max, no extra life
        assertFalse(battle.checkPhaseTriggers())
    }

    @Test
    fun `boss phase does not roll back damage already dealt`() {
        val boss = World.monsterById("ancient_slime")!!
        val battle = BossBattle(boss = boss, maxLives = 3, initialLives = 3)
        val problem = ProblemGenerator.generate(Difficulty.EASY, Operation.ADD, Random(7))
        while (battle.hp > 204) {
            battle.answerCorrect(problem, 1000L)
        }
        val hpAfterPhase2 = battle.hp
        assertTrue(hpAfterPhase2 <= 204)
        assertEquals(2, battle.phase)
    }

    // ---- daily ----

    @Test
    fun `daily seed is stable per date`() {
        val date = LocalDate.of(2026, 11, 10)
        assertEquals(dailySeed(date), dailySeed(date))
        assertNotEquals(dailySeed(date), dailySeed(LocalDate.of(2026, 11, 11)))
    }

    @Test
    fun `daily problems are stable per date and count 10`() {
        val date = LocalDate.of(2026, 11, 10)
        val first = generateDailyProblems(Difficulty.MEDIUM, date)
        val second = generateDailyProblems(Difficulty.MEDIUM, date)
        assertEquals(10, first.size)
        assertEquals(first, second)
    }

    @Test
    fun `second daily attempt on the same day is rejected, next day allowed`() {
        val today = LocalDate.of(2026, 11, 10)
        val state = DailyState()
        assertTrue(state.canStartToday(today))
        val started = state.startToday(today)
        assertTrue(started.started)
        assertFalse(started.canStartToday(today))
        assertTrue(started.canStartToday(today.plusDays(1)))
    }

    @Test
    fun `daily challenge score does not depend on the sword`() {
        // The challenge path always calls the formula with swordMultiplier = 1.
        val score = dailyChallengeScore(Difficulty.EASY, 4000L)
        val neutral = computeDamage(
            Difficulty.EASY, 4000L,
            swordMultiplier = 1.0, speedCap = 1.6, typeBonus = 1.0, preciseBonus = 0
        ).total
        assertEquals(neutral, score)
        // A sword would change raw damage, so the challenge must not use it.
        val withSword = computeDamage(
            Difficulty.EASY, 4000L,
            swordMultiplier = 1.5, speedCap = 1.6, typeBonus = 1.0, preciseBonus = 0
        ).total
        assertNotEquals(withSword, score)
    }

    @Test
    fun `daily errands pick 3 of 5 deterministically`() {
        val picked = pickDailyErrands(1234L)
        assertEquals(3, picked.size)
        assertEquals(3, picked.toSet().size)
        assertTrue(DAILY_ERRAND_IDS.containsAll(picked))
        assertEquals(picked, pickDailyErrands(1234L))
    }

    // ---- quests, contracts, titles ----

    @Test
    fun `quest completes only once`() {
        val progress = QuestProgress()
        assertTrue(progress.complete("forge_level1"))
        assertFalse(progress.complete("forge_level1"))
        assertTrue(progress.isCompleted("forge_level1"))
        assertFalse(progress.isCompleted("herald_daily"))
    }

    @Test
    fun `contract fast10easy checks total time under 90 seconds`() {
        assertTrue(Contracts.isFast10Easy(List(10) { 8000L }))
        assertFalse(Contracts.isFast10Easy(List(10) { 10000L }))
        assertFalse(Contracts.isFast10Easy(List(9) { 1000L }))
        assertTrue(Contracts.isStreak15(15))
        assertFalse(Contracts.isStreak15(14))
        assertTrue(Contracts.isDuelBossWon(1, true))
        assertFalse(Contracts.isDuelBossWon(2, true))
    }

    @Test
    fun `title thresholds`() {
        assertEquals("title_apprentice", Titles.titleFor(0))
        assertEquals("title_apprentice", Titles.titleFor(999))
        assertEquals("title_squire", Titles.titleFor(1000))
        assertEquals("title_knight", Titles.titleFor(5000))
        assertEquals("title_master", Titles.titleFor(15000))
        assertEquals("title_archmage", Titles.titleFor(30000))
        assertEquals("title_archmage", Titles.titleFor(99999))
    }

    // ---- forge ----

    @Test
    fun `forge prices and level cap`() {
        val rusty = Equipment.swordById("rusty")!!
        val caravan = Equipment.swordById("caravan")!!
        assertEquals(50, Forge.forgePrice(rusty, 0))
        assertEquals(100, Forge.forgePrice(rusty, 1))
        assertEquals(100, Forge.forgePrice(caravan, 0))
        assertEquals(5, Forge.MAX_LEVEL)
        assertNull(Forge.startForge("rusty", 5))
        val attempt = Forge.startForge("rusty", 0)!!
        assertEquals(1, attempt.targetLevel)
    }

    @Test
    fun `forge success needs 5 correct, failure at 3 wrong`() {
        var attempt = Forge.startForge("rusty", 0)!!
        repeat(4) { attempt = Forge.recordForgeAnswer(attempt, true) }
        assertEquals(ForgeResult.IN_PROGRESS, Forge.resultOf(attempt))
        attempt = Forge.recordForgeAnswer(attempt, true)
        assertEquals(ForgeResult.SUCCESS, Forge.resultOf(attempt))

        var failed = Forge.startForge("rusty", 0)!!
        repeat(2) { failed = Forge.recordForgeAnswer(failed, false) }
        assertEquals(ForgeResult.IN_PROGRESS, Forge.resultOf(failed))
        failed = Forge.recordForgeAnswer(failed, false)
        assertEquals(ForgeResult.FAILED, Forge.resultOf(failed))
    }

    @Test
    fun `failed forge does not deduct gold, success deducts exactly the price`() {
        val rusty = Equipment.swordById("rusty")!!
        var gold = 500
        // Failed attempt: no apply call happens, gold is untouched.
        var attempt = Forge.startForge("rusty", 0)!!
        repeat(3) { attempt = Forge.recordForgeAnswer(attempt, false) }
        assertEquals(ForgeResult.FAILED, Forge.resultOf(attempt))
        assertEquals(500, gold)

        // Successful forge deducts the price once.
        val result = Forge.applyForgeSuccess(gold, rusty, 1, emptyMap())!!
        gold = result.newGold
        assertEquals(450, gold)
        assertEquals(mapOf("rusty" to 1), result.newForgeLevels)

        // Cannot afford, target above max, or backwards level: null.
        assertNull(Forge.applyForgeSuccess(40, rusty, 1, emptyMap()))
        assertNull(Forge.applyForgeSuccess(10000, rusty, 6, emptyMap()))
        assertNull(Forge.applyForgeSuccess(10000, rusty, 1, mapOf("rusty" to 1)))
    }

    // ---- training, economy, world ----

    @Test
    fun `training difficulty unlocks by xp`() {
        assertEquals(Difficulty.EASY, Training.unlockedDifficulty(0))
        assertEquals(Difficulty.EASY, Training.unlockedDifficulty(499))
        assertEquals(Difficulty.MEDIUM, Training.unlockedDifficulty(500))
        assertEquals(Difficulty.MEDIUM, Training.unlockedDifficulty(1499))
        assertEquals(Difficulty.HARD, Training.unlockedDifficulty(1500))
    }

    @Test
    fun `economy rewards`() {
        val slime = World.monsterById("slime_add")!!
        assertEquals(25, Economy.goldForMonster(slime))
        assertEquals(25, Economy.xpForMonster(slime))
        assertEquals(Reward(100, 50), Economy.dailyReward)
        assertEquals(Reward(300, 0), Economy.recordChestReward)
        assertEquals(Reward(30, 20), Economy.errandReward)
        assertEquals(Reward(150, 0), Economy.contractReward)
    }

    @Test
    fun `world has 5 regions, throne hall has only a boss`() {
        assertEquals(5, World.regions.size)
        val throne = World.regionById(5)!!
        assertTrue(throne.monsters.isEmpty())
        assertEquals("archchancellor", throne.boss.id)
        assertEquals(3, throne.boss.phases)
        assertEquals(Difficulty.HARD, throne.difficulty)
        assertEquals(3, World.bossPhases("remainder_keeper").size)
        assertEquals(Difficulty.HARD, World.bossPhases("remainder_keeper")[2].difficulty)
    }

    // ---- save / load ----

    @Test
    fun `save survives serialize and deserialize`() {
        val save = SaveData(
            gold = 1234,
            xp = 777,
            totalDamage = 54321L,
            unlockedRegion = 3,
            ownedSwords = setOf("rusty", "apprentice", "caravan"),
            equippedSword = "caravan",
            forgeLevels = mapOf("rusty" to 2, "apprentice" to 1),
            armorId = "leather",
            amuletId = "hourglass",
            completedQuests = setOf("forge_level1", "herald_daily"),
            daily = DailyState(LocalDate.of(2026, 11, 10), true),
            hallOfFame = listOf(RunRecord(1500L, 2, "2026-11-09")),
            weeklyTournament = WeeklyTournamentState("2026-W45", 9000L),
            currentStreak = 7,
            bestStreak = 15,
            totalSolved = 120,
            totalCorrect = 110
        )
        val result = deserialize(serialize(save))
        assertFalse(result.wasReset)
        assertEquals(save, result.save)
    }

    @Test
    fun `unknown version or broken json resets without throwing`() {
        val json = serialize(SaveData(gold = 50))
        val wrongVersion = json.replace("\"version\":1", "\"version\":99")
        val reset1 = deserialize(wrongVersion)
        assertTrue(reset1.wasReset)
        assertEquals(SaveData(), reset1.save)

        val reset2 = deserialize("{this is not json")
        assertTrue(reset2.wasReset)
        assertEquals(SaveData(), reset2.save)

        val reset3 = deserialize("")
        assertTrue(reset3.wasReset)
        assertEquals(SaveData(), reset3.save)
    }
}
