package com.mathblade.app
import com.mathblade.R

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mathblade.core.CombatAnimationState
import com.mathblade.core.Equipment
import com.mathblade.core.MathProblem
import com.mathblade.core.Operation

private val Ink = Color(0xFF1A1420)
private val Paper = Color(0xFFF2EAD8)
private val Gold = Color(0xFFE6B84A)
private val Muted = Color(0xFFA99B7E)

/**
 * Shared answer area: one field, or two (quotient + remainder) for
 * remainder problems. A blank / non-numeric answer is not an attempt:
 * the ViewModel ignores it without spending a life.
 */
@Composable
fun AnswerFields(
    problem: MathProblem,
    onSubmit: (String?, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var answer by remember(problem) { mutableStateOf("") }
    var quotient by remember(problem) { mutableStateOf("") }
    var remainder by remember(problem) { mutableStateOf("") }
    Column(modifier = modifier) {
        if (problem.operation == Operation.REMAINDER) {
            Text(
                text = stringResource(R.string.problem_remainder_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quotient,
                    onValueChange = { quotient = it },
                    label = { Text(stringResource(R.string.battle_quotient_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = remainder,
                    onValueChange = { remainder = it },
                    label = { Text(stringResource(R.string.battle_remainder_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text(stringResource(R.string.battle_answer_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                if (problem.operation == Operation.REMAINDER) {
                    onSubmit(null, quotient, remainder)
                } else {
                    onSubmit(answer, null, null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.battle_answer_button))
        }
    }
}

@Composable
fun BattleScreen(vm: GameViewModel, navController: NavController, regionId: Int) {
    val snapshot by vm.battle.collectAsState()
    val runResult by vm.runResult.collectAsState()
    val save by vm.save.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(runResult) {
        when (runResult) {
            is RunResult.Death -> navController.navigate("death") {
                popUpTo("camp") { inclusive = false }
            }
            is RunResult.Victory -> navController.navigate("victory") {
                popUpTo("camp") { inclusive = false }
            }
            null -> Unit
        }
    }

    BackHandler(enabled = snapshot != null) { showExitDialog = true }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.battle_exit_title)) },
            text = { Text(stringResource(R.string.battle_exit_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    vm.leaveBattle()
                    navController.popBackStack("camp", false)
                }) { Text(stringResource(R.string.battle_exit_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.battle_exit_stay))
                }
            }
        )
    }

    val snap = snapshot
    if (snap == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.common_loading), color = Paper)
        }
        return
    }

    val displayAnimation = if (snap.feedback == null) CombatAnimationState.IDLE else snap.animation
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .imePadding()
    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ink.copy(alpha = 0.82f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(monsterNameRes(snap.monster.id)),
                    color = Paper,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (snap.isBoss) {
                    Text(
                        text = stringResource(R.string.monster_boss_label) +
                            " · " + stringResource(R.string.battle_boss_phase, snap.phase),
                        color = Gold
                    )
                }
                Text(
                    text = stringResource(R.string.battle_monster_counter, snap.monsterIndex + 1, snap.monsterTotal),
                    color = Muted
                )
                Spacer(modifier = Modifier.height(6.dp))
                HpBar(current = snap.monsterHp, max = snap.monsterMaxHp)
                HeartsRow(lives = snap.lives, maxLives = snap.maxLives)
                if (snap.phaseBannerPhase != null) {
                    Text(
                        text = stringResource(R.string.battle_phase_changed) +
                            " " + stringResource(R.string.battle_boss_phase, snap.phaseBannerPhase),
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            PixelBackdrop(
                place = pixelPlaceFor(regionBackgroundRes(regionId)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                BattleArena(
                    monsterId = snap.monster.id,
                    armorId = save.armorId,
                    swordId = save.equippedSword,
                    animation = displayAnimation,
                    showHit = snap.feedback is BattleFeedback.Correct,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
                    .background(Ink.copy(alpha = 0.92f))
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                val problem = snap.problem
                if (snap.fallen != null) {
                    Text(
                        text = stringResource(
                            R.string.battle_fallen_title,
                            stringResource(monsterNameRes(snap.fallen.monsterId))
                        ),
                        color = Paper,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(
                            R.string.battle_fallen_reward,
                            snap.fallen.gold,
                            snap.fallen.xp
                        ),
                        color = Gold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { vm.continueAfterFallen() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.battle_next_monster)) }
                } else if (problem != null) {
                    Text(
                        text = formatProblem(context, problem),
                        color = Paper,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(
                            R.string.battle_school_label,
                            stringResource(operationNameRes(problem.operation))
                        ),
                        color = Muted
                    )
                    when (val fb = snap.feedback) {
                        is BattleFeedback.Correct -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.battle_correct) + " −" + fb.breakdown.total,
                                color = Gold,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = fb.breakdown.breakdownText, color = Paper)
                        }
                        is BattleFeedback.Wrong -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.battle_wrong),
                                color = Color(0xFFE07060),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        null -> Unit
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AnswerFields(
                        problem = problem,
                        onSubmit = { a, q, r -> vm.submitBattleAnswer(a, q, r) }
                    )
                    if (Equipment.amuletRevealsLastDigit(save.amuletId)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (snap.hintDigit != null) {
                            Text(stringResource(R.string.battle_hint_text, snap.hintDigit), color = Gold)
                        } else if (!snap.hintUsed) {
                            OutlinedButton(
                                onClick = { vm.useHint() },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(stringResource(R.string.battle_hint_button)) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showExitDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.common_to_camp)) }
            }
    }
}

@Composable
fun DeathScreen(vm: GameViewModel, navController: NavController) {
    val result = (vm.runResult.collectAsState().value as? RunResult.Death)
    FullscreenBackground(R.drawable.bg_camp) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.death_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = stringResource(R.string.death_text))
            if (result != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(R.string.death_run_damage, result.runDamage))
                Text(
                    text = stringResource(R.string.battle_fallen_reward, result.runGold, result.runXp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.popBackStack("camp", false) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.common_to_camp)) }
        }
    }
}

@Composable
fun VictoryScreen(vm: GameViewModel, navController: NavController) {
    val result = (vm.runResult.collectAsState().value as? RunResult.Victory)
    FullscreenBackground(R.drawable.bg_camp) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.victory_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = stringResource(R.string.victory_text))
            if (result != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        R.string.victory_reward,
                        result.runGold,
                        result.runXp
                    )
                )
                Text(text = stringResource(R.string.victory_run_damage, result.runDamage))
                Spacer(modifier = Modifier.height(8.dp))
                if (result.nextRegionId != null) {
                    Text(
                        text = stringResource(
                            R.string.victory_region_unlocked,
                            stringResource(regionNameRes(result.nextRegionId))
                        )
                    )
                } else {
                    Text(text = stringResource(R.string.victory_last_region))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.popBackStack("camp", false) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.common_to_camp)) }
        }
    }
}
