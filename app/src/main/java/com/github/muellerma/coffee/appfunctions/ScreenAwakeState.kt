package com.github.muellerma.coffee.appfunctions

import androidx.annotation.Keep
import androidx.appfunctions.AppFunctionSerializable

/**
 * Represents the current state of the device screen wakefulness.
 */
@Keep
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ScreenAwakeState(
    /** Whether the screen is currently being held awake. */
    val isAwake: Boolean,
    /** Remaining seconds until automatic turn-off, or null if indefinite or inactive. */
    val remainingSeconds: Long?,
    /** Brightness policy: "BRIGHT" (stays bright) or "DIM" (dims on inactivity without sleeping). */
    val mode: String
)
