# Architecture

## Components
- **MainActivity**: Entry point, UI for manual toggle and settings instigation.
- **ForegroundService**: The core service that keeps the application alive. It manages the WakeLock and the ongoing notification.
- **ProgressNotificationManager**: Specialized manager for Android 16+ "Baklava" progress-centric notifications.
- **Prefs**: Wrapper around SharedPreferences for easy access to settings.
- **CoffeeApplication**: Application class, handles dependency injection-like global state (observers).

## Flows
- **Start/Stop**: Triggered via Activity or Notification actions.
- **Timeout**: Managed by a Coroutine in `ForegroundService`.
- **Refresh Timeout**: A new action that resets the current timeout countdown without changing the duration preference.
