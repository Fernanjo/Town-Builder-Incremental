package com.cosytown.builder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cosytown.builder.ui.build.BuildMenuScreen
import com.cosytown.builder.ui.legacyshop.LegacyShopScreen
import com.cosytown.builder.ui.prestige.PrestigeScreen
import com.cosytown.builder.ui.town.TownScreen
import com.cosytown.engine.BuildingType
import com.cosytown.engine.GameState
import com.cosytown.engine.LegacyUpgradeId

@Composable
fun AppNavHost(
    navController: NavHostController,
    state: GameState,
    onBuyBuilding: (BuildingType) -> Unit,
    onPrestige: () -> Unit,
    onBuyLegacyUpgrade: (LegacyUpgradeId) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = AppDestination.TOWN.route, modifier = modifier) {
        composable(AppDestination.TOWN.route) {
            TownScreen(
                state = state,
                onNavigateToBuild = { navController.navigate(AppDestination.BUILD.route) },
                onNavigateToPrestige = { navController.navigate(AppDestination.PRESTIGE.route) },
            )
        }
        composable(AppDestination.BUILD.route) {
            BuildMenuScreen(state = state, onBuy = onBuyBuilding)
        }
        composable(AppDestination.PRESTIGE.route) {
            PrestigeScreen(state = state, onConfirmPrestige = onPrestige)
        }
        composable(AppDestination.LEGACY_SHOP.route) {
            LegacyShopScreen(state = state, onBuyUpgrade = onBuyLegacyUpgrade)
        }
    }
}
