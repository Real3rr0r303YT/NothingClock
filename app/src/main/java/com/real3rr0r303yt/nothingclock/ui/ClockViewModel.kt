/*
 * NothingClock – private clock, alarm, timer & stopwatch for Android
 * Copyright (C) 2026 Real3rr0r303YT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.real3rr0r303yt.nothingclock.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.real3rr0r303yt.nothingclock.clockApp
import com.real3rr0r303yt.nothingclock.core.Alarm
import com.real3rr0r303yt.nothingclock.core.CountdownTimer
import com.real3rr0r303yt.nothingclock.core.Stopwatch
import com.real3rr0r303yt.nothingclock.data.ClockSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Shared state for all four tabs. Writes go through the app scope so they always complete. */
class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application.clockApp
    private val repo = app.repository
    private val scheduler = app.scheduler

    val alarms: StateFlow<List<Alarm>?> = repo.alarms.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val stopwatch: StateFlow<Stopwatch> = repo.stopwatch.stateIn(viewModelScope, SharingStarted.Eagerly, Stopwatch())
    val timer: StateFlow<CountdownTimer> = repo.timer.stateIn(viewModelScope, SharingStarted.Eagerly, CountdownTimer())
    val zones: StateFlow<List<String>> = repo.worldZones.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val settings: StateFlow<ClockSettings> = repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, ClockSettings())

    // ------------------------------------------------------------------ alarms

    /** Saves and schedules [alarm]; returns the trigger time via [onScheduled] (for the "rings in" hint). */
    fun saveAlarm(alarm: Alarm, onScheduled: (Long?) -> Unit = {}) {
        app.appScope.launch {
            val saved = repo.saveAlarm(alarm)
            val trigger = scheduler.schedule(saved)
            viewModelScope.launch { onScheduled(trigger) }
        }
    }

    fun setAlarmEnabled(alarm: Alarm, enabled: Boolean, onScheduled: (Long?) -> Unit = {}) =
        saveAlarm(alarm.copy(enabled = enabled), onScheduled)

    fun deleteAlarm(alarm: Alarm) {
        scheduler.cancel(alarm.id)
        app.appScope.launch { repo.deleteAlarm(alarm.id) }
    }

    fun newAlarmDefaults(hour: Int, minute: Int): Alarm = settings.value.let {
        Alarm(0, hour, minute, vibrate = it.vibrate, fadeInSeconds = it.fadeInSeconds, snoozeMinutes = it.snoozeMinutes)
    }

    // ------------------------------------------------------------------ stopwatch

    private fun updateStopwatch(transform: (Stopwatch, Long) -> Stopwatch) {
        val next = transform(stopwatch.value, SystemClock.elapsedRealtime())
        app.appScope.launch { repo.setStopwatch(next) }
    }

    fun stopwatchStart() = updateStopwatch { s, now -> s.start(now) }
    fun stopwatchPause() = updateStopwatch { s, now -> s.pause(now) }
    fun stopwatchLap() = updateStopwatch { s, now -> s.lap(now) }
    fun stopwatchReset() = updateStopwatch { s, _ -> s.reset() }

    // ------------------------------------------------------------------ timer

    private fun updateTimer(transform: (CountdownTimer, Long) -> CountdownTimer) {
        val next = transform(timer.value, System.currentTimeMillis())
        next.endsAt?.let(scheduler::scheduleTimer) ?: scheduler.cancelTimer()
        app.appScope.launch { repo.setTimer(next) }
    }

    fun timerStart(durationMs: Long) {
        if (durationMs > 0) updateTimer { t, now -> t.start(durationMs, now) }
    }

    fun timerPause() = updateTimer { t, now -> t.pause(now) }
    fun timerResume() = updateTimer { t, now -> t.resume(now) }
    fun timerAddMinute() = updateTimer { t, now -> t.addTime(60_000, now) }
    fun timerReset() = updateTimer { t, _ -> t.reset() }

    // ------------------------------------------------------------------ world clock & settings

    fun addZone(id: String) = app.appScope.launch { repo.setWorldZones(zones.value + id) }
    fun removeZone(id: String) = app.appScope.launch { repo.setWorldZones(zones.value - id) }

    fun updateSettings(transform: (ClockSettings) -> ClockSettings) = app.appScope.launch { repo.updateSettings(transform) }
}
