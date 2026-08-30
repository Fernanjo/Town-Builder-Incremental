package com.cosytown.builder.ui.town

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cosytown.builder.ui.components.BuildingVisual
import com.cosytown.engine.BuildingDefs
import com.cosytown.engine.BuildingType
import com.cosytown.engine.GameState
import com.cosytown.engine.PrestigeCalculator

@Composable
fun TownScreen(
    state: GameState,
    onNavigateToBuild: () -> Unit,
    onNavigateToPrestige: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val owned = remember(state.buildings) { state.buildings.filter { it.value > 0 }.entries.toList() }
    var selectedType by remember { mutableStateOf<BuildingType?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (PrestigeCalculator.isPrestigeAvailable(state)) {
            PrestigeAvailableBanner(onNavigateToPrestige, modifier = Modifier.padding(bottom = 12.dp))
        }

        if (owned.isEmpty()) {
            EmptyTownMessage(onNavigateToBuild)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(owned, key = { it.key.name }) { (type, count) ->
                    BuildingTile(type = type, count = count, onClick = { selectedType = type })
                }
            }
        }
    }

    selectedType?.let { type ->
        BuildingInfoDialog(type = type, onDismiss = { selectedType = null })
    }
}

@Composable
private fun BuildingTile(type: BuildingType, count: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BuildingVisual(type = type, modifier = Modifier.fillMaxWidth())
            Text(
                text = BuildingDefs.of(type).displayName,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "x$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EmptyTownMessage(onNavigateToBuild: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Your town is empty. Build your first House or Market to get started.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onNavigateToBuild, modifier = Modifier.padding(top = 16.dp)) {
                Text("Open Build Menu")
            }
        }
    }
}

@Composable
private fun PrestigeAvailableBanner(onNavigateToPrestige: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onNavigateToPrestige,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Prestige is available!", style = MaterialTheme.typography.titleMedium)
            Text(
                "Your town has grown enough to start a new legacy. Tap to see the details.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun BuildingInfoDialog(type: BuildingType, onDismiss: () -> Unit) {
    val def = BuildingDefs.of(type)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss, colors = ButtonDefaults.textButtonColors()) { Text("Close") } },
        title = { Text(def.displayName) },
        text = { Text(def.description) },
    )
}
