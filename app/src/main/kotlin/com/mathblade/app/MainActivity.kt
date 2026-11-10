package com.mathblade.app
import com.mathblade.R

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay

/**
 * String routes (Navigation Compose). Back goes to the previous screen;
 * from the middle of a problem it asks for confirmation (BattleScreen).
 * screenOrientation is intentionally not fixed: the system decides.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm: GameViewModel by viewModels()
        setContent {
            MathBladeTheme {
                val loaded by vm.saveLoaded.collectAsState()
                val corrupted by vm.saveCorrupted.collectAsState()
                if (!loaded) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.common_loading))
                    }
                } else if (corrupted) {
                    IncompatibleSaveScreen(vm)
                } else {
                    MathBladeNav(vm)
                }
            }
        }
    }
}

@Composable
private fun MathBladeNav(vm: GameViewModel) {
    val navController = rememberNavController()
    val notice by vm.notice.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "camp") {
            composable("camp") { CampScreen(vm, navController) }
            composable(
                route = "battle/{regionId}",
                arguments = listOf(navArgument("regionId") { type = NavType.IntType })
            ) { backStackEntry ->
                val regionId = backStackEntry.arguments?.getInt("regionId") ?: 1
                BattleScreen(vm, navController, regionId)
            }
            composable("quests") { QuestLogScreen(vm, navController) }
            composable("daily") { DailyScreen(vm, navController) }
            composable("forge") { ForgeScreen(vm, navController) }
            composable("chest") { ChestScreen(vm, navController) }
            composable("hall") { HallScreen(vm, navController) }
            composable("settings") { SettingsScreen(vm, navController) }
            composable("death") { DeathScreen(vm, navController) }
            composable("victory") { VictoryScreen(vm, navController) }
        }
        if (notice != null) {
            LaunchedEffect(notice) {
                delay(2500)
                vm.consumeNotice()
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color(0xFF1E1A14), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(text = stringResource(notice!!))
            }
        }
    }
}
