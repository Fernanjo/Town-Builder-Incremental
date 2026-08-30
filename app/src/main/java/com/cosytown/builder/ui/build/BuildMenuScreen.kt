package com.cosytown.builder.ui.build

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosytown.builder.ui.components.BuildingVisual
import com.cosytown.builder.ui.components.formatCompact
import com.cosytown.engine.BuildingDef
import com.cosytown.engine.BuildingDefs
import com.cosytown.engine.BuildingType
import com.cosytown.engine.GameEngine
import com.cosytown.engine.GameState

@Composable
fun BuildMenuScreen(
    state: GameState,
    onBuy: (BuildingType) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(BuildingDefs.ALL, key = { it.type.name }) { def ->
            BuildingRow(def = def, state = state, onBuy = { onBuy(def.type) })
        }
    }
}

@Composable
private fun BuildingRow(def: BuildingDef, state: GameState, onBuy: () -> Unit) {
    val unlocked = BuildingDefs.isUnlocked(def.type, state.buildings)
    val owned = state.buildings[def.type] ?: 0
    val cost = GameEngine.costFor(state, def.type)
    val affordable = unlocked && GameEngine.canAfford(state, cost)

    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BuildingVisual(type = def.type, modifier = Modifier.size(56.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("${def.displayName} (x$owned)", style = MaterialTheme.typography.titleMedium)
                Text(def.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (unlocked) costLabel(cost.coins, cost.materials) else "Locked",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (affordable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onBuy,
                enabled = affordable,
                colors = ButtonDefaultsCosy(),
            ) {
                Text("Build")
            }
        }
    }
}

@Composable
private fun ButtonDefaultsCosy() = androidx.compose.material3.ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
)

private fun costLabel(coins: Double, materials: Double): String {
    val parts = mutableListOf<String>()
    if (coins > 0) parts += "${coins.formatCompact()} Coins"
    if (materials > 0) parts += "${materials.formatCompact()} Materials"
    return if (parts.isEmpty()) "Free" else parts.joinToString(" + ")
}
