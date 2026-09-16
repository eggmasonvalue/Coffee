package com.github.muellerma.coffee.tiles

import com.github.muellerma.coffee.ForegroundService

class RestartTile : CoffeeTile() {
    override val action = ForegroundService.ACTION_RESTART_TIMEOUT
    override val tileState: Int
        get() = runningState()
}
