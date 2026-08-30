package com.cosytown.builder.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.cosytown.engine.GameState
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Serializes the whole save file as JSON via kotlinx.serialization. */
object GameStateSerializer : Serializer<GameState> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val defaultValue: GameState = GameState.newRun(System.currentTimeMillis())

    override suspend fun readFrom(input: InputStream): GameState {
        val text = input.readBytes().decodeToString()
        return try {
            json.decodeFromString(GameState.serializer(), text)
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to parse GameState save file", e)
        }
    }

    override suspend fun writeTo(t: GameState, output: OutputStream) {
        output.write(json.encodeToString(GameState.serializer(), t).encodeToByteArray())
    }
}
