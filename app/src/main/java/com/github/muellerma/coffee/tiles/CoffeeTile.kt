package com.github.muellerma.coffee.tiles

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

    override fun onTileAdded() {
        super.onTileAdded()
        updateState()
    }

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

    protected fun updateState() {
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

        fun updateListeningTiles() {
            listeningTiles.toList().forEach(CoffeeTile::updateState)
        }

        fun runningState(): Int =
            if (ForegroundService.isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }
}
