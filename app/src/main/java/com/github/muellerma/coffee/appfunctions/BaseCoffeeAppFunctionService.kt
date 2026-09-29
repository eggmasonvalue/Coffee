package com.github.muellerma.coffee.appfunctions

import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.github.muellerma.coffee.ForegroundService

@AppFunctionServiceEntryPoint(
    serviceName = "CoffeeAppFunctionService",
    appFunctionXmlFileName = "coffee_app_functions"
)
abstract class BaseCoffeeAppFunctionService : AppFunctionService() {

    /**
     * Controls whether the device display is kept awake, its timeout duration, and brightness mode.
     *
     * Call this when the user asks to "keep screen awake", "keep screen on", "don't sleep",
     * "turn on Coffee", "stop Coffee", or "let screen sleep".
     *
     * @param awake Whether to keep the display awake (true) or allow normal system sleep (false).
     * @param durationMinutes Duration in minutes (0 for indefinite, or positive minutes like 5, 15, 30).
     *                        If omitted when enabling, uses the current setting. Automatically restarts countdown.
     * @param mode Brightness mode: "BRIGHT" (stays bright) or "DIM" (dims on inactivity without sleeping).
     * @return The updated screen awake state.
     */
    @AppFunction(isDescribedByKDoc = true)
    fun setScreenAwake(
        awake: Boolean,
        durationMinutes: Int? = null,
        mode: String? = null
    ): ScreenAwakeState {
        if (durationMinutes != null) {
            if (durationMinutes < 0) {
                throw AppFunctionInvalidArgumentException("Duration cannot be negative.")
            }
            if (durationMinutes > 1440) {
                throw AppFunctionInvalidArgumentException("Duration cannot exceed 24 hours (1440 minutes).")
            }
        }
        val allowDimming = when (mode?.trim()?.uppercase()) {
            "DIM" -> true
            "BRIGHT" -> false
            null -> null
            else -> throw AppFunctionInvalidArgumentException("Mode must be 'BRIGHT' or 'DIM'.")
        }

        return if (awake) {
            ForegroundService.startOrUpdate(
                context = this,
                durationMinutes = durationMinutes,
                allowDimming = allowDimming
            )
        } else {
            ForegroundService.stop(this)
        }
    }

    /**
     * Queries the current display awake state.
     *
     * Call this when the user asks "is Coffee on?", "is my screen set to stay awake?",
     * or "how much time is left on the screen timer?".
     *
     * @return The current screen awake state.
     */
    @AppFunction(isDescribedByKDoc = true)
    fun getScreenAwakeStatus(): ScreenAwakeState {
        return ForegroundService.currentState(this)
    }
}
