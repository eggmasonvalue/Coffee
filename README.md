# Coffee

Coffee keeps the display awake on Android 17 without a launcher activity or settings screen.

Add any of its controls from the Quick Settings editor:

- **Coffee** — turn Coffee on or off
- **Next timeout** — select the next timeout, starting Coffee at five minutes when off
- **Restart timer** — restart the current timeout, starting Coffee when off
- **Dimming** — toggle whether Android may dim the display normally

While Coffee is active, the same timeout and dimming controls are available in its Live Update notification. The dimming button shows **Dim** when it will enable normal inactivity dimming and **Bright** when it will keep the display bright. It does not dim the display immediately, and tapping it resets Android's inactivity timer. When dimming is allowed, wait for the normal screen timeout without touching the display: the screen may dim, but Coffee prevents it from turning off.

Tapping the notification itself turns Coffee off. Locking the display also stops Coffee.

This fork intentionally targets only Android 17 (API 37).
