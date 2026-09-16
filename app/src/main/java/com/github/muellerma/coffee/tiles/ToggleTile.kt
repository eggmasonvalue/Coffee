package com.github.muellerma.coffee.tiles

import com.github.muellerma.coffee.ForegroundService

class ToggleTile : CoffeeTile() {
    override val action = ForegroundService.ACTION_TOGGLE
    override val tileState: Int
        get() = runningState()
}
