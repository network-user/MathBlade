package com.mathblade.app
import com.mathblade.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mathblade.core.Contracts
import com.mathblade.core.Equipment
import com.mathblade.core.Forge
import com.mathblade.core.ForgeResult
import com.mathblade.core.Quests
import com.mathblade.core.Training
import com.mathblade.core.todayInMoscow

@Composable
private fun ScreenColumn(titleRes: Int, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = GameColors.ink
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

// ---- Quest log ----

@Composable
fun QuestLogScreen(vm: GameViewModel, navController: NavController) {
    FullscreenBackground(R.drawable.bg_camp) {
        ScreenColumn(R.string.quest_log_title, onBack = { navController.popBackStack() }) {
            SectionTitle(stringResource(R.string.quest_main_title))
            for (quest in Quests.mainQuests) {
                QuestRow(quest.id, vm.isQuestCompleted(quest.id))
            }
            SectionTitle(stringResource(R.string.quest_side_title))
            for (quest in Quests.sideQuests) {
                QuestRow(quest.id, vm.isQuestCompleted(quest.id))
            }
            SectionTitle(stringResource(R.string.quest_contracts_title))
            for (contract in Contracts.all) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = gameCardColors()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(contractNameRes(contract.id)))
                        Text(
                            text = stringResource(R.string.contract_reward),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GameColors.muted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestRow(questId: String, done: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = gameCardColors()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(questNameRes(questId)),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(if (done) R.string.quest_done else R.string.quest_active),
                color = if (done) Color(0xFF5E9B4C) else Color(0xFFD9A441)
            )
        }
    }
}

// ---- Daily challenge ----

@Composable
fun DailyScreen(vm: GameViewModel, navController: NavController) {
    val save by vm.save.collectAsState()
    val daily by vm.daily.collectAsState()
    val context = LocalContext.current
    val today = todayInMoscow()
    val canStart = save.daily.canStartToday(today)

    FullscreenBackground(R.drawable.bg_camp) {
        ScreenColumn(R.string.daily_title, onBack = { navController.popBackStack() }) {
            val state = daily
            when {
                state == null -> {
                    Text(text = stringResource(R.string.daily_desc))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.daily_difficulty_label,
                            stringResource(difficultyNameRes(Training.unlockedDifficulty(save.xp)))
                        )
                    )
                    SectionTitle(stringResource(R.string.daily_errands_title))
                    for (errand in vm.errandsToday()) {
                        Text(text = "• " + stringResource(errandNameRes(errand)))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (canStart) {
                        Button(
                            onClick = { vm.startDaily() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.daily_start)) }
                    } else {
                        Text(text = stringResource(R.string.daily_already_done))
                    }
                }
                state.finished -> {
                    Text(
                        text = stringResource(R.string.daily_finished_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.daily_finished_text, state.score))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            vm.closeDaily()
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.common_to_camp)) }
                }
                else -> {
                    Text(text = stringResource(R.string.daily_progress, state.index + 1))
                    Text(text = stringResource(R.string.daily_score, state.score))
                    val problem = state.problem
                    if (problem != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = formatProblem(context, problem),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AnswerFields(
                            problem = problem,
                            onSubmit = { a, q, r -> vm.submitDailyAnswer(a, q, r) }
                        )
                    }
                }
            }
        }
    }
}

// ---- Forge ----

@Composable
fun ForgeScreen(vm: GameViewModel, navController: NavController) {
    val save by vm.save.collectAsState()
    val forge by vm.forge.collectAsState()
    val context = LocalContext.current

    FullscreenBackground(R.drawable.bg_camp) {
        ScreenColumn(R.string.forge_title, onBack = { navController.popBackStack() }) {
            val state = forge
            if (state == null) {
                Text(text = stringResource(R.string.forge_desc))
                SectionTitle(stringResource(R.string.forge_choose_sword))
                for (sword in Equipment.swords) {
                    val level = save.forgeLevels[sword.id] ?: 0
                    val owned = vm.isSwordOwned(sword.id)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = gameCardColors()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            FramedImage(
                                imageRes = swordDrawableRes(sword.id),
                                contentDescription = stringResource(swordNameRes(sword.id)),
                                height = 120.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(swordNameRes(sword.id)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = stringResource(swordDescRes(sword.id)))
                            Text(text = stringResource(R.string.forge_level, level))
                            Text(
                                text = stringResource(
                                    R.string.sword_multiplier,
                                    sword.effectiveMultiplier(level).toString()
                                )
                            )
                            if (!owned) {
                                Text(
                                    text = stringResource(
                                        R.string.sword_obtained_region,
                                        sword.regionObtained
                                    )
                                )
                            } else if (level >= Forge.MAX_LEVEL) {
                                Text(text = stringResource(R.string.forge_max_level))
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(
                                        R.string.forge_price,
                                        Forge.forgePrice(sword, level)
                                    )
                                )
                                Button(onClick = { vm.startForge(sword.id) }) {
                                    Text(stringResource(R.string.forge_start))
                                }
                            }
                        }
                    }
                }
            } else {
                val sword = Equipment.swordById(state.swordId)
                if (sword != null) {
                    Text(
                        text = stringResource(
                            R.string.forge_school,
                            sword.typeBonusPercent.keys.firstOrNull()?.let {
                                stringResource(operationNameRes(it))
                            } ?: stringResource(operationNameRes(com.mathblade.core.Operation.ADD))
                        )
                    )
                }
                Text(text = stringResource(R.string.forge_need_correct, state.attempt.correctCount))
                Text(text = stringResource(R.string.forge_wrong, state.attempt.wrongCount))
                Text(text = stringResource(R.string.forge_price, state.price))
                when (state.finished) {
                    ForgeResult.SUCCESS -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.forge_success, state.attempt.targetLevel),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { vm.cancelForge() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.common_to_camp)) }
                    }
                    ForgeResult.FAILED -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.forge_failed),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { vm.cancelForge() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.common_to_camp)) }
                    }
                    ForgeResult.IN_PROGRESS -> {
                        val problem = state.problem
                        if (problem != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = formatProblem(context, problem),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.lastCorrect != null) {
                                Text(
                                    text = stringResource(
                                        if (state.lastCorrect) R.string.battle_correct else R.string.battle_wrong
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            AnswerFields(
                                problem = problem,
                                onSubmit = { a, q, r -> vm.submitForgeAnswer(a, q, r) }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { vm.cancelForge() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.forge_cancel)) }
                    }
                }
            }
        }
    }
}

// ---- Chest ----

@Composable
fun ChestScreen(vm: GameViewModel, navController: NavController) {
    val save by vm.save.collectAsState()
    val ownedArmors by vm.ownedArmors.collectAsState()
    val ownedAmulets by vm.ownedAmulets.collectAsState()

    FullscreenBackground(R.drawable.bg_camp) {
        ScreenColumn(R.string.chest_title, onBack = { navController.popBackStack() }) {
            GoldXpRow(gold = save.gold, xp = save.xp)

            SectionTitle(stringResource(R.string.chest_record_title))
            Text(text = stringResource(R.string.chest_record_text))
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { vm.claimRecordChest() },
                enabled = save.hallOfFame.isNotEmpty() && "chest_record" !in save.completedQuests,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.chest_record_claim)) }

            SectionTitle(stringResource(R.string.chest_sword_slot))
            for (sword in Equipment.swords) {
                val owned = vm.isSwordOwned(sword.id)
                val equipped = save.equippedSword == sword.id
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = gameCardColors()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        FramedImage(
                            imageRes = swordDrawableRes(sword.id),
                            contentDescription = stringResource(swordNameRes(sword.id)),
                            height = 100.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(swordNameRes(sword.id)))
                        Text(
                            text = if (owned) {
                                stringResource(R.string.chest_owned)
                            } else {
                                stringResource(R.string.sword_not_owned)
                            }
                        )
                        if (owned && !equipped) {
                            Button(onClick = { vm.equipSword(sword.id) }) {
                                Text(stringResource(R.string.chest_equip))
                            }
                        }
                        if (equipped) Text(text = stringResource(R.string.chest_equipped))
                    }
                }
            }

            SectionTitle(stringResource(R.string.chest_armor_slot))
            for (armor in Equipment.armors) {
                val owned = armor.id in ownedArmors
                val equipped = save.armorId == armor.id
                val locked = armor.requiredRegionCleared > 0 &&
                    save.unlockedRegion <= armor.requiredRegionCleared
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = gameCardColors()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        FramedImage(
                            imageRes = armorDrawableRes(armor.id),
                            contentDescription = stringResource(armorNameRes(armor.id)),
                            height = 100.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(armorNameRes(armor.id)))
                        Text(text = stringResource(armorDescRes(armor.id)))
                        Text(text = stringResource(R.string.armor_lives, armor.maxLives))
                        when {
                            equipped -> Text(text = stringResource(R.string.chest_equipped))
                            owned -> Button(onClick = { vm.equipArmor(armor.id) }) {
                                Text(stringResource(R.string.chest_equip))
                            }
                            locked -> Text(
                                text = stringResource(
                                    R.string.chest_locked_region,
                                    armor.requiredRegionCleared
                                )
                            )
                            else -> Button(onClick = { vm.buyArmor(armor.id) }) {
                                Text(stringResource(R.string.chest_buy, armor.price))
                            }
                        }
                    }
                }
            }

            SectionTitle(stringResource(R.string.chest_amulet_slot))
            for (amulet in Equipment.amulets) {
                val owned = amulet.id in ownedAmulets
                val equipped = save.amuletId == amulet.id
                val locked = amulet.requiredRegionCleared > 0 &&
                    save.unlockedRegion <= amulet.requiredRegionCleared
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = gameCardColors()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        FramedImage(
                            imageRes = amuletDrawableRes(amulet.id),
                            contentDescription = stringResource(amuletNameRes(amulet.id)),
                            height = 100.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(amuletNameRes(amulet.id)))
                        Text(text = stringResource(amuletDescRes(amulet.id)))
                        when {
                            equipped -> Button(onClick = { vm.equipAmulet(amulet.id) }) {
                                Text(stringResource(R.string.chest_equipped))
                            }
                            owned -> Button(onClick = { vm.equipAmulet(amulet.id) }) {
                                Text(stringResource(R.string.chest_equip))
                            }
                            locked -> Text(
                                text = stringResource(
                                    R.string.chest_locked_region,
                                    amulet.requiredRegionCleared
                                )
                            )
                            else -> Button(onClick = { vm.buyAmulet(amulet.id) }) {
                                Text(stringResource(R.string.chest_buy, amulet.price))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---- Hall of Fame ----

@Composable
fun HallScreen(vm: GameViewModel, navController: NavController) {
    val save by vm.save.collectAsState()

    FullscreenBackground(R.drawable.bg_camp) {
        ScreenColumn(R.string.hall_title, onBack = { navController.popBackStack() }) {
            Text(text = stringResource(R.string.hall_total_damage, save.totalDamage))
            SectionTitle(stringResource(R.string.hall_top_runs))
            if (save.hallOfFame.isEmpty()) {
                Text(
                    text = stringResource(R.string.hall_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = stringResource(R.string.hall_empty_text))
            } else {
                val sorted = save.hallOfFame.sortedByDescending { it.damage }.take(10)
                for (record in sorted) {
                    Text(
                        text = stringResource(
                            R.string.hall_run_item,
                            record.damage,
                            stringResource(regionNameRes(record.regionId)),
                            record.date
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val bestDaily = vm.bestDaily7Days()
            Text(
                text = if (bestDaily > 0) {
                    stringResource(R.string.hall_best_daily, bestDaily)
                } else {
                    stringResource(R.string.hall_best_daily_none)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle(stringResource(R.string.hall_weekly))
            val weekly = save.weeklyTournament
            if (weekly.weekId == vm.currentWeekId() && weekly.bestDamage > 0) {
                Text(text = stringResource(R.string.hall_weekly_best, weekly.bestDamage))
                Text(text = stringResource(R.string.hall_weekly_yours, weekly.bestDamage))
            } else {
                Text(text = stringResource(R.string.hall_weekly_none))
            }
        }
    }
}

// ---- Settings ----

@Composable
fun SettingsScreen(vm: GameViewModel, navController: NavController) {
    val haptics by vm.hapticsEnabled.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_title)) },
            text = { Text(stringResource(R.string.settings_reset_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    vm.resetSave()
                    navController.popBackStack("camp", false)
                }) { Text(stringResource(R.string.settings_reset_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    FullscreenBackground(R.drawable.bg_camp) {
        ScreenColumn(R.string.settings_title, onBack = { navController.popBackStack() }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.settings_haptics))
                    Text(
                        text = stringResource(R.string.settings_haptics_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GameColors.muted
                    )
                }
                Switch(checked = haptics, onCheckedChange = { vm.setHaptics(it) })
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(R.string.settings_reset))
            Text(
                text = stringResource(R.string.settings_reset_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = GameColors.muted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.settings_reset)) }
        }
    }
}

// ---- Incompatible save ----

@Composable
fun IncompatibleSaveScreen(vm: GameViewModel) {
    FullscreenBackground(R.drawable.bg_camp) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.incompatible_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = stringResource(R.string.incompatible_text))
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { vm.resetCorruptedSave() },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.incompatible_start_new)) }
        }
    }
}
