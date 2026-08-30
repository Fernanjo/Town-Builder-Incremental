package com.cosytown.builder.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(val route: String, val label: String, val icon: ImageVector) {
    TOWN("town", "Town", Icons.Filled.Home),
    BUILD("build", "Build", Icons.Filled.Add),
    PRESTIGE("prestige", "Prestige", Icons.Filled.AutoAwesome),
    LEGACY_SHOP("legacy_shop", "Legacy", Icons.Filled.AccountBalance),
}
