package com.cosytown.builder

import android.app.Application
import com.cosytown.builder.data.GameStateRepository

class CosyTownApplication : Application() {
    val gameStateRepository: GameStateRepository by lazy { GameStateRepository(this) }
}
