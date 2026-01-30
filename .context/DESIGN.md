# Design

## Core Functionality
Coffee keeps the screen awake using a Foreground Service and WakeLock (or alternate mode modifying system settings).

## Notification
The foreground service shows a notification with actions:
- Next timeout
- Refresh timeout (Resets the timer to the current timeout value)

On Android 16+, it uses Progress Notifications managed by `ProgressNotificationManager`.

## Quick Settings Tiles
- **Toggle Coffee**: Turns the service on or off.
- **Next Timeout**: Cycles through available timeout options.
- **Refresh Timeout**: Resets the timer to the current timeout value.
