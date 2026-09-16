package com.github.muellerma.coffee.tiles

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.VibratorManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.github.muellerma.coffee.ForegroundService
import java.util.Collections
import java.util.WeakHashMap

abstract class CoffeeTile : TileService() {
    protected abstract val action: String
    protected abstract val tileState: Int

    override fun onStartListening() {
        super.onStartListening()
        listeningTiles.add(this)
        updateState()
    }

    override fun onStopListening() {
        listeningTiles.remove(this)
        super.onStopListening()
    }

    override fun onDestroy() {
        listeningTiles.remove(this)
        super.onDestroy()
    }

    private fun updateState() {
        qsTile?.apply {
            state = tileState
            subtitle = null
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        performClickHaptic()
        startForegroundService(Intent(this, ForegroundService::class.java).setAction(action))
    }

    protected fun performClickHaptic() {
        getSystemService(VibratorManager::class.java).defaultVibrator.vibrate(
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        )
    }

    companion object {
        private val listeningTiles = Collections.newSetFromMap(WeakHashMap<CoffeeTile, Boolean>())
        private val tileClasses = arrayOf(
            ToggleTile::class.java,
            TimeoutTile::class.java,
            RestartTile::class.java,
            DimmingTile::class.java
        )

        fun requestStateUpdates(context: Context) {
            // requestListeningState() may do nothing when SystemUI already has a tile bound.
            // Update bound instances synchronously, then request bindings for the rest.
            listeningTiles.toList().forEach(CoffeeTile::updateState)
            tileClasses.forEach {
                TileService.requestListeningState(context, ComponentName(context, it))
            }
        }

        fun runningState(): Int =
            if (ForegroundService.isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }
}
