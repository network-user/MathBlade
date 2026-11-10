package com.mathblade.core

import java.security.MessageDigest
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

/**
 * Daily challenge. Seed construction follows the DotMathBot idea
 * (SHA-256 of a namespaced date string, first 8 hex digits as a Long),
 * with the "mathblade-daily-" namespace; Kotlin Random sequences differ
 * from Python's, only stability inside MathBlade matters.
 *
 * One attempt per calendar day: the attempt is spent at the moment the
 * player starts ([DailyState.startToday]), not when they finish.
 */
fun dailySeed(date: LocalDate): Long {
    val text = "mathblade-daily-" + date.toString() // YYYY-MM-DD
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(text.toByteArray(Charsets.UTF_8))
    val hex = digest.take(4).joinToString("") { "%02x".format(it.toInt() and 0xFF) }
    return hex.toLong(16)
}

/** Current date in Moscow time, the day boundary for daily content. */
fun todayInMoscow(): LocalDate = LocalDate.now(ZoneId.of("Europe/Moscow"))

val ALL_OPERATIONS: List<Operation> = Operation.entries.toList()

/**
 * 10 mixed problems for the day. Difficulty never exceeds what the
 * player has unlocked. Deterministic for a given date: regenerating
 * from the seed gives the same list, so only seed + date need storing.
 */
fun generateDailyProblems(
    unlockedDifficulty: Difficulty,
    date: LocalDate = todayInMoscow(),
    rng: Random = Random(dailySeed(date))
): List<MathProblem> =
    ProblemGenerator.generateList(unlockedDifficulty, ALL_OPERATIONS, 10, rng)

/**
 * Daily challenge score: the same damage formula, but always with
 * neutral multipliers, so the score does not depend on the sword.
 */
fun dailyChallengeScore(difficulty: Difficulty, elapsedMs: Long): Int =
    computeDamage(
        difficulty = difficulty,
        elapsedMs = elapsedMs,
        swordMultiplier = 1.0,
        speedCap = 1.6,
        typeBonus = 1.0,
        preciseBonus = 0
    ).total

/** Fixed errand ids; [pickDailyErrands] picks 3 of the 5 by seed. */
val DAILY_ERRAND_IDS: List<String> = listOf(
    "defeat_school",
    "solve_10",
    "flawless_battle",
    "upgrade_sword",
    "finish_daily"
)

fun pickDailyErrands(seed: Long): List<String> =
    DAILY_ERRAND_IDS.shuffled(Random(seed)).take(3)

data class DailyState(
    val attemptDate: LocalDate? = null,
    val started: Boolean = false
) {
    fun canStartToday(today: LocalDate): Boolean = attemptDate != today

    /** Marks the attempt as spent at the moment of starting. */
    fun startToday(today: LocalDate): DailyState = copy(attemptDate = today, started = true)
}
