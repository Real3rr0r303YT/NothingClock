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

package com.real3rr0r303yt.nothingclock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.real3rr0r303yt.nothingclock.clockApp
import com.real3rr0r303yt.nothingclock.core.CountdownTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Fired by AlarmManager. Starts the ringing service and plans the next occurrence. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.clockApp
        scope.launch {
            try {
                when (intent.action) {
                    ACTION_ALARM -> {
                        val id = intent.getIntExtra(EXTRA_ALARM_ID, -1)
                        val alarm = app.repository.alarmsNow().firstOrNull { it.id == id } ?: return@launch
                        if (!alarm.enabled && !intent.getBooleanExtra(EXTRA_SNOOZED, false)) return@launch
                        // Plan ahead right away: repeating alarms get their next slot, one-shots turn off.
                        if (alarm.repeats) {
                            app.scheduler.schedule(alarm)
                        } else if (!intent.getBooleanExtra(EXTRA_SNOOZED, false)) {
                            app.repository.saveAlarm(alarm.copy(enabled = false))
                        }
                        ContextCompat.startForegroundService(context, RingService.alarmIntent(context, alarm))
                    }
                    ACTION_TIMER -> {
                        val timer = app.repository.timerNow()
                        if (!timer.isRunning) return@launch
                        app.repository.setTimer(CountdownTimer())
                        val vibrate = app.repository.settingsNow().timerVibrate
                        ContextCompat.startForegroundService(context, RingService.timerIntent(context, vibrate))
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_ALARM = "com.real3rr0r303yt.nothingclock.action.ALARM"
        const val ACTION_TIMER = "com.real3rr0r303yt.nothingclock.action.TIMER"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_SNOOZED = "snoozed"

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
