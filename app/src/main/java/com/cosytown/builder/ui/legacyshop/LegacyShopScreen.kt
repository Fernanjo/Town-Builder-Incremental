package com.cosytown.builder.ui.legacyshop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.cosytown.builder.ui.components.formatCompact
import com.cosytown.engine.GameState
import com.cosytown.engine.LegacyUpgradeDef
import com.cosytown.engine.LegacyUpgradeDefs
import com.cosytown.engine.LegacyUpgradeId

@Composable
fun LegacyShopScreen(
    state: GameState,
    onBuyUpgrade: (LegacyUpgradeId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Legacy Shop", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Legacy Points: ${state.legacy.legacyPoints.formatCompact()}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(LegacyUpgradeDefs.ALL, key = { it.id.name }) { def ->
                LegacyUpgradeRow(
                    def = def,
                    currentLevel = state.legacy.purchasedLevels[def.id] ?: 0,
                    legacyPoints = state.legacy.legacyPoints,
                    onBuy = { onBuyUpgrade(def.id) },
                )
            }
        }
    }
}

@Composable
private fun LegacyUpgradeRow(
    def: LegacyUpgradeDef,
    currentLevel: Int,
    legacyPoints: Long,
    onBuy: () -> Unit,
) {
    val maxed = currentLevel >= def.maxLevel
    val nextCost = if (maxed) null else def.costForNextLevel(currentLevel)
    val affordable = nextCost != null && legacyPoints >= nextCost

    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${def.displayName} (${currentLevel}/${def.maxLevel})", style = MaterialTheme.typography.titleMedium)
                Text(def.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (maxed) "Maxed out" else "Cost: ${nextCost?.formatCompact()} Legacy Points",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (affordable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(onClick = onBuy, enabled = affordable) {
                Text(if (maxed) "Maxed" else "Upgrade")
            }
        }
    }
}
