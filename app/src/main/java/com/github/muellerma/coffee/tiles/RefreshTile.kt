package com.github.muellerma.coffee.tiles

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.github.muellerma.coffee.ForegroundService
import com.github.muellerma.coffee.ServiceStatus
import com.github.muellerma.coffee.ServiceStatusObserver
import com.github.muellerma.coffee.coffeeApp

@RequiresApi(Build.VERSION_CODES.N)
class RefreshTile : AbstractTile() {
    override fun onClick() {
        Log.d(TAG, "onClick()")
        when (coffeeApp().lastStatusUpdate) {
            is ServiceStatus.Stopped -> {
                // If stopped, start it
                ForegroundService.changeState(this, ForegroundService.Companion.STATE.START, false)
            }
            is ServiceStatus.Running -> {
                // If running, refresh timeout
                val intent = Intent(this, ForegroundService::class.java).apply {
                    action = ForegroundService.ACTION_REFRESH_TIMEOUT
                }
                startService(intent)
            }
        }
        super.onClick()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    companion object {
        private val TAG = RefreshTile::class.java.simpleName

        fun requestTileStateUpdate(context: Context) {
            Log.d(TAG, "requestTileStateUpdate()")
            try {
                requestListeningState(context, ComponentName(context, RefreshTile::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Error when calling requestListeningState()", e)
            }
        }
    }

    class TileServiceStatusObserver(private val context: Context) : ServiceStatusObserver {
        override fun onServiceStatusUpdate(status: ServiceStatus) {
            requestTileStateUpdate(context)
        }
    }
}
