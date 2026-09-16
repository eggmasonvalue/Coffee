package com.github.muellerma.coffee

import android.content.Context

object CoffeeSettings {
    private const val KEY_TIMEOUT = "timeout"
    private const val KEY_ALLOW_DIMMING = "allow_dimming"
    private val timeouts = intArrayOf(0, 5, 15, 30, 60, 120)

    private fun preferences(context: Context) =
        context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

    fun timeoutMinutes(context: Context): Int =
        preferences(context).getString(KEY_TIMEOUT, "0")
            ?.toIntOrNull()
            ?.takeIf(timeouts::contains)
            ?: 0

    fun selectNextTimeout(context: Context, startAtFiveMinutes: Boolean = false): Int {
        val current = timeoutMinutes(context)
        val next = if (startAtFiveMinutes) 5 else timeouts[(timeouts.indexOf(current) + 1) % timeouts.size]
        preferences(context).edit().putString(KEY_TIMEOUT, next.toString()).apply()
        return next
    }

    fun allowsDimming(context: Context): Boolean =
        preferences(context).getBoolean(KEY_ALLOW_DIMMING, true)

    fun toggleDimming(context: Context): Boolean {
        val enabled = !allowsDimming(context)
        preferences(context).edit().putBoolean(KEY_ALLOW_DIMMING, enabled).apply()
        return enabled
    }
}
