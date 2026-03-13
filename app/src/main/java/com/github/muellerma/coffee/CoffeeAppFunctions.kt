package com.github.muellerma.coffee

import android.content.Context
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AppFunctions for Coffee.
 */
class CoffeeAppFunctions {

    /**
     * Turns on the screen awake mode.
     *
     * @param appFunctionContext The execution context.
     */
    @AppFunction
    fun turnOn(appFunctionContext: AppFunctionContext): CoffeeStatus {
        ForegroundService.changeState(appFunctionContext.context, ForegroundService.Companion.STATE.START, false)
        return getStatus(appFunctionContext)
    }

    /**
     * Turns off the screen awake mode.
     *
     * @param appFunctionContext The execution context.
     */
    @AppFunction
    fun turnOff(appFunctionContext: AppFunctionContext): CoffeeStatus {
        ForegroundService.changeState(appFunctionContext.context, ForegroundService.Companion.STATE.STOP, false)
        return getStatus(appFunctionContext)
    }

    /**
     * Toggles the screen awake mode.
     *
     * @param appFunctionContext The execution context.
     */
    @AppFunction
    fun toggle(appFunctionContext: AppFunctionContext): CoffeeStatus {
        ForegroundService.changeState(appFunctionContext.context, ForegroundService.Companion.STATE.TOGGLE, false)
        return getStatus(appFunctionContext)
    }

    /**
     * Gets the current status of the screen awake mode.
     *
     * @param appFunctionContext The execution context.
     */
    @AppFunction
    fun getStatus(appFunctionContext: AppFunctionContext): CoffeeStatus {
        val app = appFunctionContext.context.applicationContext as CoffeeApplication
        val status = app.lastStatusUpdate
        val isRunning = status is ServiceStatus.Running
        val remainingSeconds = if (status is ServiceStatus.Running) status.remaining?.inWholeSeconds else null
        return CoffeeStatus(isRunning, remainingSeconds ?: 0)
    }

    /**
     * Sets the timeout for the screen awake mode in minutes.
     *
     * @param appFunctionContext The execution context.
     * @param minutes The timeout in minutes. Use 0 for infinite timeout.
     */
    @AppFunction
    fun setTimeout(appFunctionContext: AppFunctionContext, minutes: Int): CoffeeStatus {
        val prefs = Prefs(appFunctionContext.context)
        prefs.timeout = minutes
        return getStatus(appFunctionContext)
    }
}

/**
 * The status of the Coffee app.
 */
@AppFunctionSerializable
data class CoffeeStatus(
    /** Indicates if the screen awake mode is currently running. */
    val isRunning: Boolean,
    /** The remaining time in seconds until the screen awake mode automatically turns off. 0 if there is no timeout. */
    val remainingSeconds: Long
)
