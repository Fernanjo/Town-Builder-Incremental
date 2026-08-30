package com.cosytown.builder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cosytown.builder.ui.components.ResourceBar
import com.cosytown.builder.ui.navigation.AppDestination
import com.cosytown.builder.ui.navigation.AppNavHost
import com.cosytown.builder.ui.theme.CosyTownBuilderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CosyTownBuilderTheme {
                val app = application as CosyTownApplication
                val viewModel: GameViewModel = viewModel(
                    factory = GameViewModelFactory(app.gameStateRepository),
                )
                CosyTownApp(viewModel)
            }
        }
    }
}

@Composable
private fun CosyTownApp(viewModel: GameViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.persistNow() }

    val state by viewModel.uiState.collectAsState()
    val currentState = state

    if (currentState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ResourceBar(state = currentState, modifier = Modifier.padding(16.dp))
            AppNavHost(
                navController = navController,
                state = currentState,
                onBuyBuilding = viewModel::buyBuilding,
                onPrestige = viewModel::prestige,
                onBuyLegacyUpgrade = viewModel::buyLegacyUpgrade,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
