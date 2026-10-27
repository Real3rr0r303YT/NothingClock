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

package com.real3rr0r303yt.nothingclock

import android.app.Application
import android.content.Context
import com.real3rr0r303yt.nothingclock.alarm.AlarmScheduler
import com.real3rr0r303yt.nothingclock.alarm.Notifications
import com.real3rr0r303yt.nothingclock.data.ClockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NothingClockApp : Application() {

    lateinit var repository: ClockRepository
        private set
    lateinit var scheduler: AlarmScheduler
        private set

    /** Outlives activities and services, for short writes that must complete. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        repository = ClockRepository(this)
        scheduler = AlarmScheduler(this)
        Notifications.createChannels(this)
    }
}

val Context.clockApp: NothingClockApp get() = applicationContext as NothingClockApp
