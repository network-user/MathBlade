package com.mathblade.app
import com.mathblade.R

import android.app.Application
import android.content.Context
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mathblade.core.ALL_OPERATIONS
import com.mathblade.core.BattleState
import com.mathblade.core.BossBattle
import com.mathblade.core.CombatAnimationState
import com.mathblade.core.DamageBreakdown
import com.mathblade.core.Economy
import com.mathblade.core.Equipment
import com.mathblade.core.Forge
import com.mathblade.core.ForgeAttempt
import com.mathblade.core.ForgeResult
import com.mathblade.core.MathProblem
import com.mathblade.core.MonsterDef
import com.mathblade.core.Operation
import com.mathblade.core.ProblemGenerator
import com.mathblade.core.RegionDef
import com.mathblade.core.RunRecord
import com.mathblade.core.SaveData
import com.mathblade.core.Training
import com.mathblade.core.WeeklyTournamentState
import com.mathblade.core.World
import com.mathblade.core.checkAnswer
import com.mathblade.core.checkRemainderAnswer
import com.mathblade.core.dailyChallengeScore
import com.mathblade.core.dailySeed
import com.mathblade.core.deserialize
import com.mathblade.core.generateDailyProblems
import com.mathblade.core.parseAnswer
import com.mathblade.core.pickDailyErrands
import com.mathblade.core.serialize
import com.mathblade.core.todayInMoscow
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

private val Context.dataStore by preferencesDataStore(name = "mathblade")

private val KEY_SAVE_JSON = stringPreferencesKey("save_json")
private val KEY_VERSION = intPreferencesKey("version")
private val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
private val KEY_DAILY_BEST = stringPreferencesKey("daily_best_json")
private val KEY_OWNED_ARMORS = stringPreferencesKey("owned_armors")
private val KEY_OWNED_AMULETS = stringPreferencesKey("owned_amulets")

sealed interface BattleFeedback {
    data class Correct(val breakdown: DamageBreakdown) : BattleFeedback
    data object Wrong : BattleFeedback
}

data class FallenInfo(
    val monsterId: String,
    val gold: Int,
    val xp: Int,
    val isBoss: Boolean
)

data class BattleSnapshot(
    val regionId: Int,
    val monsterIndex: Int,
    val monsterTotal: Int,
    val monster: MonsterDef,
    val isBoss: Boolean,
    val monsterHp: Int,
    val monsterMaxHp: Int,
    val lives: Int,
    val maxLives: Int,
    val phase: Int,
    val phaseBannerPhase: Int?,
    val problem: MathProblem?,
    val feedback: BattleFeedback?,
    val fallen: FallenInfo?,
    val hintUsed: Boolean,
    val hintDigit: Int?,
    val animation: CombatAnimationState,
    val runDamage: Long
)

sealed interface RunResult {
    data class Death(val runDamage: Long, val runGold: Int, val runXp: Int) : RunResult
    data class Victory(
        val regionId: Int,
        val runDamage: Long,
        val runGold: Int,
        val runXp: Int,
        val nextRegionId: Int?
    ) : RunResult
}

data class ForgeUiState(
    val swordId: String,
    val attempt: ForgeAttempt,
    val problem: MathProblem?,
    val price: Int,
    val lastCorrect: Boolean?,
    val finished: ForgeResult
)

data class DailyUiState(
    val index: Int,
    val problem: MathProblem?,
    val score: Int,
    val correctCount: Int,
    val finished: Boolean,
    val difficulty: com.mathblade.core.Difficulty
)

/**
 * Single ViewModel for the game: it only calls :core and holds screen
 * state. Problem time is measured here (SystemClock) and ready-made
 * milliseconds are passed to :core.
 *
 * Hourglass amulet: Equipment.amuletTimeNormMultiplier exists in :core
 * (1.25), so the norm is applied by passing elapsedMs / 1.25 to
 * BattleState.answerCorrect — equivalent to normSeconds * 1.25, because
 * speed = norm * 1000 / elapsed. The daily score stays neutral: the
 * hourglass is NOT applied to dailyChallengeScore.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val rng = Random.Default

    // ---- save ----
    private val _save = MutableStateFlow(SaveData())
    val save: StateFlow<SaveData> = _save.asStateFlow()
    private val _saveLoaded = MutableStateFlow(false)
    val saveLoaded: StateFlow<Boolean> = _saveLoaded.asStateFlow()
    private val _saveCorrupted = MutableStateFlow(false)
    val saveCorrupted: StateFlow<Boolean> = _saveCorrupted.asStateFlow()

    private val _haptics = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _haptics.asStateFlow()

    /** App-side daily best scores (date to score), last 7 days. */
    private val _dailyBest = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val dailyBest: StateFlow<List<Pair<String, Int>>> = _dailyBest.asStateFlow()

    /** Owned armors / amulets: :core SaveData has no owned sets for them. */
    private val _ownedArmors = MutableStateFlow(setOf("canvas"))
    val ownedArmors: StateFlow<Set<String>> = _ownedArmors.asStateFlow()
    private val _ownedAmulets = MutableStateFlow(emptySet<String>())
    val ownedAmulets: StateFlow<Set<String>> = _ownedAmulets.asStateFlow()

    // ---- battle ----
    private val _battle = MutableStateFlow<BattleSnapshot?>(null)
    val battle: StateFlow<BattleSnapshot?> = _battle.asStateFlow()
    private val _runResult = MutableStateFlow<RunResult?>(null)
    val runResult: StateFlow<RunResult?> = _runResult.asStateFlow()

    // ---- forge / daily ----
    private val _forge = MutableStateFlow<ForgeUiState?>(null)
    val forge: StateFlow<ForgeUiState?> = _forge.asStateFlow()
    private val _daily = MutableStateFlow<DailyUiState?>(null)
    val daily: StateFlow<DailyUiState?> = _daily.asStateFlow()

    // ---- one-shot notice (string resource id) ----
    private val _notice = MutableStateFlow<Int?>(null)
    val notice: StateFlow<Int?> = _notice.asStateFlow()
    fun consumeNotice() { _notice.value = null }

    private var battleState: BattleState? = null
    private var bossBattle: BossBattle? = null
    private var queue: List<MonsterDef> = emptyList()
    private var battleRegion: RegionDef? = null
    private var monsterIndex = 0
    private var currentProblem: MathProblem? = null
    private var problemShownAtMs = 0L
    private var feedback: BattleFeedback? = null
    private var fallen: FallenInfo? = null
    private var hintUsed = false
    private var hintDigit: Int? = null
    private var phaseBannerPhase: Int? = null
    private var runDamage = 0L
    private var runGold = 0
    private var runXp = 0
    private var runWrong = 0
    private var livesCarried = 3
    private var battleGen = 0

    private var dailyProblems: List<MathProblem> = emptyList()
    private var dailyShownAtMs = 0L

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            _haptics.value = prefs[KEY_HAPTICS] ?: true
            _dailyBest.value = parseDailyBest(prefs[KEY_DAILY_BEST])
            _ownedArmors.value = parseCsv(prefs[KEY_OWNED_ARMORS]).ifEmpty { setOf("canvas") }
            _ownedAmulets.value = parseCsv(prefs[KEY_OWNED_AMULETS])
            val json = prefs[KEY_SAVE_JSON]
            if (json == null) {
                _save.value = SaveData()
            } else {
                val result = deserialize(json)
                val versionOk = prefs[KEY_VERSION]?.let { it == SaveData.CURRENT_VERSION } ?: true
                if (result.wasReset || !versionOk) {
                    // Unknown version / broken JSON: show the "start anew"
                    // screen instead of crashing or silently resetting.
                    _saveCorrupted.value = true
                }
                _save.value = result.save
            }
            _saveLoaded.value = true
        }
    }

    // ---- save helpers ----

    private fun updateSave(transform: (SaveData) -> SaveData) {
        var next = transform(_save.value)
        if (next.gold >= 500 && "treasurer_500" !in next.completedQuests) {
            next = next.copy(completedQuests = next.completedQuests + "treasurer_500")
        }
        _save.value = next
        persistSave(next)
    }

    private fun persistSave(save: SaveData) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_SAVE_JSON] = serialize(save)
                prefs[KEY_VERSION] = SaveData.CURRENT_VERSION
            }
        }
    }

    private fun persistMeta() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_DAILY_BEST] = formatDailyBest(_dailyBest.value)
                prefs[KEY_OWNED_ARMORS] = _ownedArmors.value.joinToString(",")
                prefs[KEY_OWNED_AMULETS] = _ownedAmulets.value.joinToString(",")
                prefs[KEY_HAPTICS] = _haptics.value
            }
        }
    }

    fun setHaptics(enabled: Boolean) {
        _haptics.value = enabled
        persistMeta()
    }

    fun resetSave() {
        battleGen++
        battleState = null
        bossBattle = null
        _battle.value = null
        _runResult.value = null
        _forge.value = null
        _daily.value = null
        _dailyBest.value = emptyList()
        _ownedArmors.value = setOf("canvas")
        _ownedAmulets.value = emptySet()
        _saveCorrupted.value = false
        _save.value = SaveData()
        viewModelScope.launch {
            dataStore.edit { it.clear() }
            persistSave(SaveData())
        }
    }

    /** Called from the incompatible-save screen: wipe the broken save. */
    fun resetCorruptedSave() = resetSave()

    // ---- derived helpers ----

    fun equippedSword() = Equipment.swordById(_save.value.equippedSword) ?: Equipment.swords.first()
    fun equippedArmor() = Equipment.armorById(_save.value.armorId) ?: Equipment.armors.first()
    fun isSwordOwned(swordId: String): Boolean {
        val sword = Equipment.swordById(swordId) ?: return false
        return sword.regionObtained <= _save.value.unlockedRegion
    }

    fun isQuestCompleted(questId: String): Boolean {
        if (questId.startsWith("main_region_")) {
            val region = questId.removePrefix("main_region_").toIntOrNull() ?: return false
            return _save.value.unlockedRegion > region || questId in _save.value.completedQuests
        }
        return questId in _save.value.completedQuests
    }

    fun currentQuestId(): String? {
        val save = _save.value
        if (save.unlockedRegion <= 5) {
            val mainId = "main_region_${save.unlockedRegion}"
            if (!isQuestCompleted(mainId)) return mainId
        }
        return com.mathblade.core.Quests.sideQuests
            .firstOrNull { !isQuestCompleted(it.id) }?.id
    }

    fun regionCleared(regionId: Int): Boolean = _save.value.unlockedRegion > regionId

    fun currentWeekId(): String {
        val date = LocalDate.now(ZoneId.of("Europe/Moscow"))
        val fields = WeekFields.ISO
        val year = date.get(fields.weekBasedYear())
        val week = date.get(fields.weekOfWeekBasedYear())
        return "%04d-W%02d".format(year, week)
    }

    fun errandsToday(): List<String> = pickDailyErrands(dailySeed(todayInMoscow()))

    // ---- battle ----

    fun startBattle(regionId: Int) {
        val region = World.regionById(regionId) ?: return
        if (regionId > _save.value.unlockedRegion) return
        battleRegion = region
        queue = region.monsters + region.boss
        monsterIndex = 0
        runDamage = 0L
        runGold = 0
        runXp = 0
        runWrong = 0
        livesCarried = equippedArmor().maxLives
        battleGen++
        _runResult.value = null
        startMonster(0)
    }

    private fun startMonster(index: Int) {
        val region = battleRegion ?: return
        val monster = queue.getOrNull(index) ?: return
        monsterIndex = index
        val sword = equippedSword()
        val level = _save.value.forgeLevels[sword.id] ?: 0
        val swordMultiplier = sword.effectiveMultiplier(level)
        val speedCap = sword.effectiveSpeedCap()
        val precise = sword.preciseBonus
        fallen = null
        feedback = null
        hintUsed = false
        hintDigit = null
        if (monster.isBoss) {
            battleState = null
            bossBattle = BossBattle(
                boss = monster,
                maxLives = equippedArmor().maxLives,
                initialLives = livesCarried,
                swordMultiplier = swordMultiplier,
                speedCap = speedCap,
                preciseBonus = precise
            )
        } else {
            bossBattle = null
            battleState = BattleState(
                maxLives = equippedArmor().maxLives,
                monsterHp = monster.hp,
                difficulty = region.difficulty,
                swordMultiplier = swordMultiplier,
                speedCap = speedCap,
                preciseBonus = precise,
                initialLives = livesCarried
            )
        }
        nextProblem()
        emitBattle()
    }

    private fun nextProblem() {
        val boss = bossBattle
        val problem = if (boss != null) {
            ProblemGenerator.generateMixed(boss.currentDifficulty, boss.operationsForCurrentPhase(), rng)
        } else {
            val region = battleRegion ?: return
            val monster = queue.getOrNull(monsterIndex) ?: return
            ProblemGenerator.generate(region.difficulty, monster.operation, rng)
        }
        currentProblem = problem
        problemShownAtMs = SystemClock.elapsedRealtime()
        feedback = null
        hintDigit = null
    }

    fun submitBattleAnswer(answerText: String?, quotientText: String?, remainderText: String?): Boolean {
        val problem = currentProblem ?: return false
        if (fallen != null) return false
        val correct: Boolean = if (problem.operation == Operation.REMAINDER) {
            val q = parseAnswer(quotientText) ?: return false
            val r = parseAnswer(remainderText) ?: return false
            checkRemainderAnswer(problem, q, r)
        } else {
            val a = parseAnswer(answerText) ?: return false
            checkAnswer(problem, a)
        }
        phaseBannerPhase = null
        val elapsed = SystemClock.elapsedRealtime() - problemShownAtMs
        // Hourglass: norm x1.25 via elapsedMs / 1.25 (see class doc).
        val normMultiplier = Equipment.amuletTimeNormMultiplier(_save.value.amuletId)
        val effectiveElapsed = (elapsed / normMultiplier).toLong().coerceAtLeast(1L)
        val sword = equippedSword()
        if (correct) {
            val boss = bossBattle
            val previousPhase = boss?.phase ?: 1
            val breakdown = battleState?.answerCorrect(problem, effectiveElapsed, sword.typeBonusFor(problem.operation))
                ?: boss?.answerCorrect(problem, effectiveElapsed, sword.typeBonusFor(problem.operation))
                ?: return false
            runDamage += breakdown.total
            updateSave {
                it.copy(
                    totalDamage = it.totalDamage + breakdown.total,
                    totalSolved = it.totalSolved + 1,
                    totalCorrect = it.totalCorrect + 1,
                    currentStreak = it.currentStreak + 1,
                    bestStreak = maxOf(it.bestStreak, it.currentStreak + 1)
                )
            }
            if (boss != null && boss.phase > previousPhase) {
                phaseBannerPhase = boss.phase
            }
            feedback = BattleFeedback.Correct(breakdown)
            vibrateCorrect()
        } else {
            battleState?.answerWrong() ?: bossBattle?.answerWrong()
            runWrong++
            updateSave { it.copy(totalSolved = it.totalSolved + 1, currentStreak = 0) }
            feedback = BattleFeedback.Wrong
            vibrateWrong()
        }
        emitBattle()
        val gen = battleGen
        viewModelScope.launch {
            delay(700)
            if (gen != battleGen) return@launch
            advanceAfterFeedback()
        }
        return true
    }

    private fun advanceAfterFeedback() {
        val dead = battleState?.isEnemyDead == true || bossBattle?.isEnemyDead == true
        val over = battleState?.isRunOver == true || bossBattle?.isRunOver == true
        if (over) {
            endRun()
            return
        }
        if (dead) {
            val monster = queue.getOrNull(monsterIndex) ?: return
            val gold = Economy.goldForMonster(monster)
            val xp = Economy.xpForMonster(monster)
            runGold += gold
            runXp += xp
            livesCarried = battleState?.lives ?: bossBattle?.lives ?: livesCarried
            updateSave { it.copy(gold = it.gold + gold, xp = it.xp + xp) }
            if (monster.isBoss) {
                finishVictory()
            } else {
                fallen = FallenInfo(monster.id, gold, xp, false)
                currentProblem = null
                emitBattle()
            }
            return
        }
        nextProblem()
        emitBattle()
    }

    fun continueAfterFallen() {
        fallen = null
        startMonster(monsterIndex + 1)
    }

    /** Leave mid-run: rewards for fallen monsters are already saved. */
    fun leaveBattle() {
        battleGen++
        battleState = null
        bossBattle = null
        _battle.value = null
    }

    fun useHint(): Boolean {
        val problem = currentProblem ?: return false
        if (!_save.value.amuletId.let { Equipment.amuletRevealsLastDigit(it) }) return false
        if (hintUsed || fallen != null) return false
        hintUsed = true
        // Last digit of the answer; for remainder problems the answer
        // is the quotient.
        hintDigit = ((problem.answer % 10) + 10) % 10
        emitBattle()
        return true
    }

    private fun endRun() {
        val regionId = battleRegion?.id ?: 1
        addRunRecord(runDamage, regionId)
        _runResult.value = RunResult.Death(runDamage, runGold, runXp)
        battleGen++
        battleState = null
        bossBattle = null
        _battle.value = null
    }

    private fun finishVictory() {
        val regionId = battleRegion?.id ?: 1
        val nextRegion = if (regionId < 5) regionId + 1 else null
        val quests = _save.value.completedQuests.toMutableSet()
        quests += "main_region_$regionId"
        if (regionId == 2 && runWrong == 0) quests += "mentor_region2_flawless"
        updateSave {
            it.copy(
                unlockedRegion = maxOf(it.unlockedRegion, nextRegion ?: it.unlockedRegion),
                completedQuests = quests
            )
        }
        addRunRecord(runDamage, regionId)
        _runResult.value = RunResult.Victory(regionId, runDamage, runGold, runXp, nextRegion)
        battleGen++
        battleState = null
        bossBattle = null
        _battle.value = null
    }

    private fun addRunRecord(damage: Long, regionId: Int) {
        val weekId = currentWeekId()
        updateSave { save ->
            val fame = (save.hallOfFame + RunRecord(damage, regionId, todayInMoscow().toString()))
                .sortedByDescending { it.damage }
                .take(10)
            val weekly = if (save.weeklyTournament.weekId == weekId) {
                save.weeklyTournament.copy(bestDamage = maxOf(save.weeklyTournament.bestDamage, damage))
            } else {
                WeeklyTournamentState(weekId, damage)
            }
            save.copy(hallOfFame = fame, weeklyTournament = weekly)
        }
    }

    private fun emitBattle() {
        val region = battleRegion ?: run { _battle.value = null; return }
        val monster = queue.getOrNull(monsterIndex) ?: run { _battle.value = null; return }
        val isBoss = monster.isBoss
        val hp = battleState?.monsterHp ?: bossBattle?.hp ?: 0
        val maxHp = battleState?.monsterMaxHp ?: bossBattle?.maxHp ?: monster.hp
        val lives = battleState?.lives ?: bossBattle?.lives ?: 0
        val maxLives = equippedArmor().maxLives
        val animation = battleState?.currentAnimation
            ?: bossBattle?.currentAnimation
            ?: CombatAnimationState.IDLE
        _battle.value = BattleSnapshot(
            regionId = region.id,
            monsterIndex = monsterIndex,
            monsterTotal = queue.size,
            monster = monster,
            isBoss = isBoss,
            monsterHp = hp,
            monsterMaxHp = maxHp,
            lives = lives,
            maxLives = maxLives,
            phase = bossBattle?.phase ?: 1,
            phaseBannerPhase = phaseBannerPhase,
            problem = currentProblem,
            feedback = feedback,
            fallen = fallen,
            hintUsed = hintUsed,
            hintDigit = hintDigit,
            animation = animation,
            runDamage = runDamage
        )
    }

    // ---- forge ----

    fun startForge(swordId: String): Boolean {
        val sword = Equipment.swordById(swordId) ?: return false
        val level = _save.value.forgeLevels[swordId] ?: 0
        val attempt = Forge.startForge(swordId, level) ?: run {
            _notice.value = R.string.forge_max_level
            return false
        }
        val price = Forge.forgePrice(sword, level)
        _forge.value = ForgeUiState(swordId, attempt, forgeProblem(sword), price, null, ForgeResult.IN_PROGRESS)
        return true
    }

    private fun forgeProblem(sword: com.mathblade.core.SwordDef): MathProblem {
        val ops = sword.typeBonusPercent.keys.toList()
            .ifEmpty { if (sword.id == "archmage") ALL_OPERATIONS else listOf(Operation.ADD) }
        val difficulty = World.regionById(sword.regionObtained)?.difficulty
            ?: com.mathblade.core.Difficulty.EASY
        return ProblemGenerator.generateMixed(difficulty, ops, rng)
    }

    fun submitForgeAnswer(answerText: String?, quotientText: String?, remainderText: String?): Boolean {
        val state = _forge.value ?: return false
        val problem = state.problem ?: return false
        if (state.finished != ForgeResult.IN_PROGRESS) return false
        val correct: Boolean = if (problem.operation == Operation.REMAINDER) {
            val q = parseAnswer(quotientText) ?: return false
            val r = parseAnswer(remainderText) ?: return false
            checkRemainderAnswer(problem, q, r)
        } else {
            val a = parseAnswer(answerText) ?: return false
            checkAnswer(problem, a)
        }
        val sword = Equipment.swordById(state.swordId) ?: return false
        val attempt = Forge.recordForgeAnswer(state.attempt, correct)
        val result = Forge.resultOf(attempt)
        when (result) {
            ForgeResult.SUCCESS -> {
                val applied = Forge.applyForgeSuccess(_save.value.gold, sword, attempt.targetLevel, _save.value.forgeLevels)
                if (applied == null) {
                    _notice.value = R.string.notice_not_enough_gold
                    _forge.value = state.copy(attempt = attempt, lastCorrect = correct, finished = ForgeResult.FAILED)
                } else {
                    val quests = _save.value.completedQuests.toMutableSet()
                    if (attempt.targetLevel == 1) quests += "forge_level1"
                    updateSave {
                        it.copy(gold = applied.newGold, forgeLevels = applied.newForgeLevels, completedQuests = quests)
                    }
                    _forge.value = state.copy(attempt = attempt, lastCorrect = correct, finished = ForgeResult.SUCCESS)
                }
            }
            ForgeResult.FAILED -> {
                _forge.value = state.copy(attempt = attempt, lastCorrect = correct, finished = ForgeResult.FAILED)
            }
            ForgeResult.IN_PROGRESS -> {
                _forge.value = state.copy(
                    attempt = attempt,
                    problem = forgeProblem(sword),
                    lastCorrect = correct,
                    finished = ForgeResult.IN_PROGRESS
                )
            }
        }
        return true
    }

    fun cancelForge() { _forge.value = null }

    // ---- daily challenge ----

    fun startDaily(): Boolean {
        val today = todayInMoscow()
        if (!_save.value.daily.canStartToday(today)) {
            _notice.value = R.string.daily_already_done
            return false
        }
        updateSave { it.copy(daily = it.daily.startToday(today)) }
        val difficulty = Training.unlockedDifficulty(_save.value.xp)
        dailyProblems = generateDailyProblems(difficulty, today)
        dailyShownAtMs = SystemClock.elapsedRealtime()
        _daily.value = DailyUiState(0, dailyProblems.firstOrNull(), 0, 0, false, difficulty)
        return true
    }

    fun submitDailyAnswer(answerText: String?, quotientText: String?, remainderText: String?): Boolean {
        val state = _daily.value ?: return false
        if (state.finished) return false
        val problem = state.problem ?: return false
        val correct: Boolean = if (problem.operation == Operation.REMAINDER) {
            val q = parseAnswer(quotientText) ?: return false
            val r = parseAnswer(remainderText) ?: return false
            checkRemainderAnswer(problem, q, r)
        } else {
            val a = parseAnswer(answerText) ?: return false
            checkAnswer(problem, a)
        }
        val elapsed = SystemClock.elapsedRealtime() - dailyShownAtMs
        // Neutral multipliers, infinite lives: hourglass is not applied.
        val gained = if (correct) dailyChallengeScore(state.difficulty, elapsed) else 0
        val score = state.score + gained
        val correctCount = state.correctCount + if (correct) 1 else 0
        if (state.index >= 9) {
            val today = todayInMoscow()
            updateSave {
                it.copy(
                    gold = it.gold + Economy.dailyReward.gold,
                    xp = it.xp + Economy.dailyReward.xp,
                    completedQuests = it.completedQuests + "herald_daily"
                )
            }
            saveDailyBest(today.toString(), score)
            _daily.value = state.copy(score = score, correctCount = correctCount, finished = true)
        } else {
            dailyShownAtMs = SystemClock.elapsedRealtime()
            _daily.value = state.copy(
                index = state.index + 1,
                problem = dailyProblems.getOrNull(state.index + 1),
                score = score,
                correctCount = correctCount
            )
        }
        return true
    }

    fun closeDaily() { _daily.value = null }

    private fun saveDailyBest(date: String, score: Int) {
        val today = todayInMoscow()
        val kept = _dailyBest.value.filter { (d, _) ->
            runCatching { LocalDate.parse(d) }.getOrNull()?.let { !it.isBefore(today.minusDays(6)) } ?: false
        }.toMutableList()
        val existing = kept.indexOfFirst { it.first == date }
        if (existing >= 0) {
            if (score > kept[existing].second) kept[existing] = date to score
        } else {
            kept += date to score
        }
        _dailyBest.value = kept.sortedByDescending { it.second }
        persistMeta()
    }

    fun bestDaily7Days(): Int = _dailyBest.value.maxOfOrNull { it.second } ?: 0

    // ---- chest ----

    fun buyArmor(armorId: String): Boolean {
        val def = Equipment.armorById(armorId) ?: return false
        if (def.requiredRegionCleared > 0 && _save.value.unlockedRegion <= def.requiredRegionCleared) return false
        if (_save.value.gold < def.price) {
            _notice.value = R.string.notice_not_enough_gold
            return false
        }
        _ownedArmors.value = _ownedArmors.value + armorId
        updateSave { it.copy(gold = it.gold - def.price, armorId = armorId) }
        persistMeta()
        _notice.value = R.string.notice_bought
        return true
    }

    fun equipArmor(armorId: String): Boolean {
        if (armorId !in _ownedArmors.value) return false
        updateSave { it.copy(armorId = armorId) }
        _notice.value = R.string.notice_equipped
        return true
    }

    fun buyAmulet(amuletId: String): Boolean {
        val def = Equipment.amuletById(amuletId) ?: return false
        if (def.requiredRegionCleared > 0 && _save.value.unlockedRegion <= def.requiredRegionCleared) return false
        if (_save.value.gold < def.price) {
            _notice.value = R.string.notice_not_enough_gold
            return false
        }
        _ownedAmulets.value = _ownedAmulets.value + amuletId
        updateSave { it.copy(gold = it.gold - def.price, amuletId = amuletId) }
        persistMeta()
        _notice.value = R.string.notice_bought
        return true
    }

    fun equipAmulet(amuletId: String): Boolean {
        if (amuletId !in _ownedAmulets.value) return false
        updateSave { it.copy(amuletId = if (_save.value.amuletId == amuletId) null else amuletId) }
        _notice.value = R.string.notice_equipped
        return true
    }

    fun equipSword(swordId: String): Boolean {
        if (!isSwordOwned(swordId)) return false
        updateSave { it.copy(equippedSword = swordId) }
        _notice.value = R.string.notice_equipped
        return true
    }

    /**
     * Record chest: 300 gold once, gated by the "chest_record" marker in
     * completedQuests — :core has no claimed flag for it.
     */
    fun claimRecordChest(): Boolean {
        if (_save.value.hallOfFame.isEmpty()) {
            _notice.value = R.string.chest_record_empty
            return false
        }
        if ("chest_record" in _save.value.completedQuests) {
            _notice.value = R.string.chest_record_claimed
            return false
        }
        updateSave {
            it.copy(
                gold = it.gold + Economy.recordChestReward.gold,
                completedQuests = it.completedQuests + "chest_record"
            )
        }
        _notice.value = R.string.notice_bought
        return true
    }

    // ---- haptics ----

    private fun vibrator(): Vibrator? =
        getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    fun vibrateCorrect() {
        if (!_haptics.value) return
        vibrator()?.takeIf { it.hasVibrator() }
            ?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun vibrateWrong() {
        if (!_haptics.value) return
        vibrator()?.takeIf { it.hasVibrator() }
            ?.vibrate(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    // ---- small parsers for app-side DataStore keys ----

    private fun parseDailyBest(raw: String?): List<Pair<String, Int>> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(";").mapNotNull { part ->
            val kv = part.split(":")
            if (kv.size == 2) {
                val score = kv[1].toIntOrNull() ?: return@mapNotNull null
                kv[0] to score
            } else null
        }
    }

    private fun formatDailyBest(list: List<Pair<String, Int>>): String =
        list.joinToString(";") { (date, score) -> "$date:$score" }

    private fun parseCsv(raw: String?): Set<String> =
        raw?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
}
