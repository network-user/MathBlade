package com.mathblade.app
import com.mathblade.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mathblade.core.Titles
import com.mathblade.core.World

@Composable
fun CampScreen(vm: GameViewModel, navController: NavController) {
    val save by vm.save.collectAsState()
    val currentQuest = vm.currentQuestId()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1420))) {
        PixelBackdrop(
            place = PixelPlace.CAMP,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            PixelHero(
                armorId = save.armorId,
                swordId = save.equippedSword,
                attacking = false,
                pixel = 8.dp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.camp_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    R.string.camp_hero_title,
                    stringResource(titleNameRes(Titles.titleFor(save.totalDamage)))
                ),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            GoldXpRow(gold = save.gold, xp = save.xp)

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.camp_current_quest),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (currentQuest != null) {
                    stringResource(questNameRes(currentQuest))
                } else {
                    stringResource(R.string.camp_no_quest)
                }
            )

            // Regions with the current quest and boss on the Gates card.
            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle(stringResource(R.string.camp_regions))
            for (region in World.regions) {
                val locked = region.id > save.unlockedRegion
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A14))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row {
                            Text(
                                text = stringResource(regionNameRes(region.id)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (locked) {
                                    stringResource(R.string.camp_region_locked)
                                } else {
                                    stringResource(R.string.camp_region_open)
                                }
                            )
                        }
                        Text(
                            text = stringResource(
                                R.string.camp_region_boss,
                                stringResource(monsterNameRes(region.boss.id))
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFA99B7E)
                        )
                        Text(
                            text = stringResource(
                                R.string.battle_difficulty_label,
                                stringResource(difficultyNameRes(region.difficulty))
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFA99B7E)
                        )
                        if (!locked) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                vm.startBattle(region.id)
                                navController.navigate("battle/${region.id}")
                            }) { Text(stringResource(R.string.camp_gate)) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            MenuCard(
                titleRes = R.string.camp_forge,
                descRes = R.string.camp_forge_desc,
                imageRes = R.drawable.blacksmith_portrait,
                onClick = { navController.navigate("forge") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuCard(
                titleRes = R.string.camp_board,
                descRes = R.string.camp_board_desc,
                imageRes = R.drawable.herald_portrait,
                onClick = { navController.navigate("quests") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuCard(
                titleRes = R.string.camp_chest,
                descRes = R.string.camp_chest_desc,
                imageRes = R.drawable.treasurer_portrait,
                onClick = { navController.navigate("chest") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuCard(
                titleRes = R.string.camp_hall,
                descRes = R.string.camp_hall_desc,
                imageRes = null,
                onClick = { navController.navigate("hall") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuCard(
                titleRes = R.string.camp_daily,
                descRes = R.string.camp_daily_desc,
                imageRes = null,
                onClick = { navController.navigate("daily") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuCard(
                titleRes = R.string.camp_settings,
                descRes = R.string.camp_settings_desc,
                imageRes = null,
                onClick = { navController.navigate("settings") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
