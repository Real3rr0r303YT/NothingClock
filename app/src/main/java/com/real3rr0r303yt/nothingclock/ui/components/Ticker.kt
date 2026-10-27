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

package com.real3rr0r303yt.nothingclock.ui.components

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.delay

/** Wall-clock epoch millis, updated right at each [periodMs] boundary (e.g. every full second). */
@Composable
fun rememberNow(periodMs: Long = 1_000): State<Long> = produceState(System.currentTimeMillis(), periodMs) {
    while (true) {
        val now = System.currentTimeMillis()
        value = now
        delay(periodMs - now % periodMs + 5)
    }
}

/** Monotonic millis (elapsedRealtime) updated every frame while [running]; one-shot otherwise. */
@Composable
fun rememberFrameClock(running: Boolean): State<Long> = produceState(SystemClock.elapsedRealtime(), running) {
    value = SystemClock.elapsedRealtime()
    while (running) {
        withFrameMillis { value = SystemClock.elapsedRealtime() }
    }
}

/** Wall-clock millis updated every frame while [running] (used for the countdown). */
@Composable
fun rememberWallFrameClock(running: Boolean): State<Long> = produceState(System.currentTimeMillis(), running) {
    value = System.currentTimeMillis()
    while (running) {
        withFrameMillis { value = System.currentTimeMillis() }
    }
}
