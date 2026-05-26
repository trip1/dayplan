# DayPlan

A modern, minimal daily task planner built with **Kotlin Multiplatform** and **Compose Multiplatform**.

Plan your day in 5-minute blocks, organize tasks by groups, and stay on track with smart notifications.

## Features

- **Timeline View** — Visual 6 AM to 11 PM schedule with colored task blocks
- **5-Minute Precision** — Plan your day down to 5-minute intervals
- **Task Groups** — Color-code tasks by category (Work, Personal, Health, Learning, or custom)
- **Drag & Drop** — Reposition tasks by dragging them on the timeline
- **Smart Ticker** — See what's next and what's ending soon, right in the header
- **Tap to Create** — Tap any empty time slot to create a task at that time
- **Notifications** — Get reminded 5 minutes before a task ends, and when the next task starts
- **Persistent Storage** — Tasks saved locally with SQLDelight
- **Cross-Platform** — Android, iOS, and Desktop (macOS, Windows, Linux)

## Screenshots

*Coming soon*

## Tech Stack

- **Kotlin Multiplatform** — Shared code across platforms
- **Compose Multiplatform** — Declarative UI
- **SQLDelight 2.x** — Type-safe SQLite database
- **kotlinx.coroutines** — Async operations
- **kotlinx.datetime** — Date/time handling
- **Material 3** — Modern design system

## Building

### Prerequisites

- JDK 21
- Android SDK (for Android builds)
- Xcode/macOS (for iOS builds)

### Build Android APK

```bash
./gradlew :composeApp:assembleDebug
```

### Run Desktop

```bash
./gradlew :composeApp:run
```

## License

MIT
