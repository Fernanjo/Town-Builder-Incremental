package com.cosytown.builder.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.cosytown.engine.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/** Single source of truth for the on-disk save file. There is only ever one save slot. */
class GameStateRepository(context: Context) {

    private val dataStore: DataStore<GameState> = DataStoreFactory.create(
        serializer = GameStateSerializer,
        produceFile = { context.applicationContext.dataStoreFile("game_state.json") },
    )

    /** Emits the current save, falling back to a fresh run if the file on disk is corrupted. */
    val gameState: Flow<GameState> = dataStore.data.catch { throwable ->
        if (throwable is CorruptionException) {
            emit(GameState.newRun(System.currentTimeMillis()))
        } else {
            throw throwable
        }
    }

    suspend fun save(state: GameState) {
        dataStore.updateData { state }
    }

    suspend fun update(transform: (GameState) -> GameState) {
        dataStore.updateData(transform)
    }
}
