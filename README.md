# Coffee

Coffee keeps the display awake on Android 17 without a launcher activity or settings screen. It can be controlled directly via Quick Settings tiles, its Live Update notification, or hands-free through Google Gemini and on-device AI assistants via Android 17 App Functions.

## Quick Settings Controls

Add any of its controls from the Quick Settings editor:

- **Coffee** — turn Coffee on or off
- **Next timeout** — select the next timeout, starting Coffee at five minutes when off
- **Restart timer** — restart the current timeout, starting Coffee when off
- **Dimming** — toggle whether Android may dim the display normally

While Coffee is active, the same timeout and dimming controls are available in its Live Update notification. The dimming button shows **Dim** when it will enable normal inactivity dimming and **Bright** when it will keep the display bright. It does not dim the display immediately, and tapping it resets Android's inactivity timer. When dimming is allowed, wait for the normal screen timeout without touching the display: the screen may dim, but Coffee prevents it from turning off.

Tapping the notification itself turns Coffee off. Locking the display also stops Coffee.

## Gemini & AI Assistant (App Functions)

Coffee integrates natively with Android 17 App Functions (`androidx.appfunctions`), exposing declarative capabilities directly to Google Gemini and system assistants.

### Example Prompts

- *"Keep my screen awake for 20 minutes"*
- *"Turn on Coffee in dim mode"*
- *"Keep screen awake indefinitely"*
- *"Is Coffee currently on?"*
- *"How much time is left on Coffee?"*
- *"Turn off Coffee"* / *"Let the screen sleep"*

### App Function Interface

Coffee registers two functions under `com.github.muellerma.coffee.appfunctions`:

1. **`setScreenAwake(awake, durationMinutes?, mode?)`**
   - **`awake`** (`Boolean`): Set to `true` to keep the screen awake, or `false` to stop.
   - **`durationMinutes`** (`Int?`, optional): Custom duration in minutes (`0` for indefinite, or `1`–`1440` minutes). Session-specific overrides apply immediately without altering the persistent Quick Settings presets.
   - **`mode`** (`String?`, optional): `"BRIGHT"` (keeps display at normal brightness) or `"DIM"` (allows inactivity dimming while keeping display awake).
   - **Returns**: Updated `ScreenAwakeState`.

2. **`getScreenAwakeStatus()`**
   - Queries the active state.
   - **Returns**: Ground-truth `ScreenAwakeState` containing:
     - `isAwake` (`Boolean`): Whether Coffee is actively keeping the display awake.
     - `remainingSeconds` (`Long?`): Exact countdown seconds remaining until auto turn-off (`null` if indefinite or inactive).
     - `mode` (`String`): Current brightness mode (`"BRIGHT"` or `"DIM"`).

---

This fork intentionally targets only Android 17 (API 37).
