package com.github.muellerma.coffee.tiles

import android.service.quicksettings.Tile
import com.github.muellerma.coffee.CoffeeSettings
import com.github.muellerma.coffee.ForegroundService

class DimmingTile : CoffeeTile() {
    override val action = ForegroundService.ACTION_TOGGLE_DIMMING
    override val tileState: Int
        get() = if (CoffeeSettings.allowsDimming(this)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    override fun onClick() {
        if (ForegroundService.isRunning) {
            super.onClick()
        } else {
            performClickHaptic()
            CoffeeSettings.toggleDimming(this)
            requestStateUpdates(this)
        }
    }
}
