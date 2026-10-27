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

package com.real3rr0r303yt.nothingclock.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.real3rr0r303yt.nothingclock.core.Alarm
import com.real3rr0r303yt.nothingclock.core.AlarmCodec
import com.real3rr0r303yt.nothingclock.core.CountdownTimer
import com.real3rr0r303yt.nothingclock.core.Stopwatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Defaults applied to new alarms and timers. */
data class ClockSettings(
    val snoozeMinutes: Int = 10,
    val fadeInSeconds: Int = 30,
    val vibrate: Boolean = true,
    val timerVibrate: Boolean = true,
)

private val Context.store: DataStore<Preferences> by preferencesDataStore(name = "clock")

/** All app state lives in one local DataStore file – no network, no accounts. */
class ClockRepository(context: Context) {

    private val store = context.applicationContext.store

    val alarms: Flow<List<Alarm>> = store.data.map { AlarmCodec.decode(it[ALARMS]).sortedWith(compareBy({ it.hour }, { it.minute })) }
    val stopwatch: Flow<Stopwatch> = store.data.map { Stopwatch.decode(it[STOPWATCH]) }
    val timer: Flow<CountdownTimer> = store.data.map { CountdownTimer.decode(it[TIMER]) }
    val worldZones: Flow<List<String>> = store.data.map { p -> p[ZONES]?.split(',')?.filter { it.isNotBlank() }.orEmpty() }
    val settings: Flow<ClockSettings> = store.data.map { p ->
        ClockSettings(
            snoozeMinutes = p[SNOOZE] ?: 10,
            fadeInSeconds = p[FADE] ?: 30,
            vibrate = p[VIBRATE] ?: true,
            timerVibrate = p[TIMER_VIBRATE] ?: true,
        )
    }

    suspend fun alarmsNow(): List<Alarm> = alarms.first()
    suspend fun timerNow(): CountdownTimer = timer.first()
    suspend fun settingsNow(): ClockSettings = settings.first()

    /** Inserts or replaces [alarm]; a new alarm (id 0) gets a fresh id. Returns the stored alarm. */
    suspend fun saveAlarm(alarm: Alarm): Alarm {
        var saved = alarm
        store.edit { p ->
            val list = AlarmCodec.decode(p[ALARMS]).toMutableList()
            if (alarm.id == 0) {
                saved = alarm.copy(id = (list.maxOfOrNull { it.id } ?: 0) + 1)
                list += saved
            } else {
                val index = list.indexOfFirst { it.id == alarm.id }
                if (index >= 0) list[index] = alarm else list += alarm
            }
            p[ALARMS] = AlarmCodec.encode(list)
        }
        return saved
    }

    suspend fun deleteAlarm(id: Int) {
        store.edit { p -> p[ALARMS] = AlarmCodec.encode(AlarmCodec.decode(p[ALARMS]).filterNot { it.id == id }) }
    }

    suspend fun setStopwatch(value: Stopwatch) {
        store.edit { it[STOPWATCH] = Stopwatch.encode(value) }
    }

    suspend fun setTimer(value: CountdownTimer) {
        store.edit { it[TIMER] = CountdownTimer.encode(value) }
    }

    suspend fun setWorldZones(zones: List<String>) {
        store.edit { it[ZONES] = zones.distinct().joinToString(",") }
    }

    suspend fun updateSettings(transform: (ClockSettings) -> ClockSettings) {
        store.edit { p ->
            val current = ClockSettings(p[SNOOZE] ?: 10, p[FADE] ?: 30, p[VIBRATE] ?: true, p[TIMER_VIBRATE] ?: true)
            val next = transform(current)
            p[SNOOZE] = next.snoozeMinutes
            p[FADE] = next.fadeInSeconds
            p[VIBRATE] = next.vibrate
            p[TIMER_VIBRATE] = next.timerVibrate
        }
    }

    private companion object {
        val ALARMS = stringPreferencesKey("alarms")
        val STOPWATCH = stringPreferencesKey("stopwatch")
        val TIMER = stringPreferencesKey("timer")
        val ZONES = stringPreferencesKey("world_zones")
        val SNOOZE = intPreferencesKey("default_snooze")
        val FADE = intPreferencesKey("default_fade")
        val VIBRATE = booleanPreferencesKey("default_vibrate")
        val TIMER_VIBRATE = booleanPreferencesKey("timer_vibrate")
    }
}
