# Changelog

## Unreleased
- Added "Refresh timeout" action to the notification.
  - Reset the timer and restart for the same period.
  - Implemented in `ForegroundService` and `ProgressNotificationManager`.
  - Added new icon `ic_baseline_refresh_24`.
- Renamed "Refresh timeout" to "Restart timer" in UI.
- Re-enabled "Enable/Disable dimming" action in notification.
- Added "Refresh Timeout" Quick Settings Tile (labeled "Restart timer").
  - Allows resetting the timer directly from Quick Settings.
  - Acts as a toggle (start service) if the service is currently stopped.
- Updated "Next timeout" Quick Settings Tile icon to a clock (`ic_baseline_access_time_24`).
