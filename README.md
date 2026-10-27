# NothingClock ●

**Private clock, alarm, timer & stopwatch for Android** – part of the fan-made Nothing ecosystem.
Pure OLED black to save battery, big dot-matrix digits, a single red accent – and no internet access at all.

> 🇩🇪 Deutsche Beschreibung weiter unten.

## Features

- **Clock & world clock** – large dot-matrix time inside a 60-dot seconds ring (the current second glows red),
  date, next alarm, and any number of world clocks with time difference. Long-press a city to remove it.
- **Alarms** – exact scheduling via `AlarmManager.setAlarmClock()` (works in Doze, shows in the status bar),
  one-off or repeating on chosen weekdays, label, vibration, **gentle wake-up** (volume fades in over
  15–60 s) and **snooze** (5–20 min). Full-screen ringing screen over the lock screen with big
  *Snooze* / *Dismiss* buttons; auto-silence after 10 minutes. Alarms survive reboots, time and time-zone changes.
- **Timer** – keypad entry (HH:MM:SS) or quick-start presets, dotted progress ring, pause/resume, +1 min.
  Rings with sound and optional vibration, even when the app is closed.
- **Stopwatch** – millisecond-accurate on the monotonic clock (immune to clock changes), centisecond display,
  lap times with best/slowest lap, keeps running in the background and across app restarts.
- **English & German** – follows the system language or switch in Settings.

## Privacy by design

- 🚫 **No `INTERNET` permission** – explicitly stripped from the merged manifest (`tools:node="remove"`);
  CI fails if it ever appears. No ads, no analytics, no accounts, no background data transfer.
- 💾 All alarms, timers and settings live in one local DataStore file; cloud backup and device transfer are disabled.

### Permissions and why

| Permission | Why |
|---|---|
| `USE_EXACT_ALARM` (13+), `SCHEDULE_EXACT_ALARM` (12/12L) | ring at the exact minute |
| `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT` | ringing notification / screen over the lock screen |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SYSTEM_EXEMPTED` | keep the alarm sound playing |
| `VIBRATE`, `WAKE_LOCK` | vibration, keep the CPU awake while ringing |
| `RECEIVE_BOOT_COMPLETED` | re-plan alarms after a reboot |

The app shows a hint with a one-tap fix if a required permission is missing.

## Project structure

```
NothingClock/
├── build.gradle.kts · settings.gradle.kts · gradle/libs.versions.toml
├── .github/workflows/android.yml               # tests, APK build, INTERNET check
└── app/src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── java/com/real3rr0r303yt/nothingclock/
    │   │   ├── NothingClockApp.kt · MainActivity.kt
    │   │   ├── core/        Alarm (next trigger, codec), Stopwatch, CountdownTimer, TimeText – pure Kotlin
    │   │   ├── data/        ClockRepository (DataStore)
    │   │   ├── alarm/       AlarmScheduler, AlarmReceiver, BootReceiver, RingService, RingActivity, Notifications
    │   │   └── ui/          theme · components (DotMatrixText, DotRing) · clock · alarms · timer · stopwatch · settings
    │   └── res/ (values, values-de, drawable, xml)
    └── test/                                   # scheduling, DST, codec, stopwatch, timer, font tests
```

## Build

Requirements: JDK 17+, Android SDK 35.

```bash
chmod +x gradlew              # only needed if the executable bit got lost (e.g. web upload)
./gradlew testDebugUnitTest   # unit tests
./gradlew assembleDebug       # app/build/outputs/apk/debug/
```

Min SDK 26 (Android 8.0), target SDK 35. Kotlin 2.1, Jetpack Compose (Material 3), DataStore.

## Support

If you like NothingClock: [☕ Buy Me a Coffee](https://www.buymeacoffee.com/3rr0r303yt)

---

## 🇩🇪 Deutsch

**NothingClock** ist Uhr, Wecker, Timer und Stoppuhr im Nothing-OS-Look – tiefschwarz für maximale
Akkuersparnis, mit großer Dot-Matrix-Anzeige und rotem Akzent.

- **Uhr & Weltuhr** mit Sekunden-Punktring, Datum, nächstem Wecker und beliebig vielen Städten.
- **Wecker** über den AlarmManager: einmalig oder an Wochentagen, Vibration, sanft steigender Ton, Schlummern,
  Vollbild-Weckscreen auf dem Sperrbildschirm – übersteht Neustarts und Zeitzonenwechsel.
- **Timer** mit Ziffernblock und Schnellstart, Ton/Vibration bei Ablauf, auch bei geschlossener App.
- **Stoppuhr** millisekundengenau mit Rundenzeiten (beste/langsamste Runde).
- **Datenschutz:** keine Internet-Berechtigung, keine Werbung, kein Tracking, keine Konten.
- **Sprachen:** Deutsch & Englisch – folgt dem System oder wird in den Einstellungen umgestellt.

## License

Copyright © 2026 **Real3rr0r303YT**

This program is free software: you can redistribute it and/or modify it under the terms of the
**GNU General Public License v3.0** as published by the Free Software Foundation, either version 3
of the License, or (at your option) any later version. See [LICENSE](LICENSE).

NothingClock is an unofficial fan project and is not affiliated with Nothing Technology Limited.
