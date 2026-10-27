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

package com.real3rr0r303yt.nothingclock.core

/**
 * Immutable stopwatch state based on a monotonic clock (elapsedRealtime), so it is
 * immune to wall-clock changes. All times are milliseconds.
 */
data class Stopwatch(
    /** Time accumulated before the current run. */
    val accumulated: Long = 0,
    /** Monotonic start of the current run, or null when paused/reset. */
    val runningSince: Long? = null,
    /** Lap split times (total elapsed at each lap), oldest first. */
    val laps: List<Long> = emptyList(),
) {
    val isRunning: Boolean get() = runningSince != null
    val isReset: Boolean get() = !isRunning && accumulated == 0L && laps.isEmpty()

    fun elapsed(now: Long): Long = accumulated + (runningSince?.let { (now - it).coerceAtLeast(0) } ?: 0)

    fun start(now: Long) = if (isRunning) this else copy(runningSince = now)

    fun pause(now: Long) = if (!isRunning) this else copy(accumulated = elapsed(now), runningSince = null)

    fun lap(now: Long) = if (!isRunning) this else copy(laps = laps + elapsed(now))

    fun reset() = Stopwatch()

    /** Individual lap durations, oldest first. */
    fun lapDurations(): List<Long> = laps.mapIndexed { i, split -> split - (laps.getOrNull(i - 1) ?: 0) }

    companion object {
        fun encode(s: Stopwatch): String =
            listOf(s.accumulated.toString(), s.runningSince?.toString().orEmpty(), s.laps.joinToString(",")).joinToString(";")

        fun decode(text: String?): Stopwatch {
            if (text.isNullOrBlank()) return Stopwatch()
            return runCatching {
                val f = text.split(';')
                Stopwatch(
                    accumulated = f[0].toLong(),
                    runningSince = f[1].takeIf { it.isNotEmpty() }?.toLong(),
                    laps = f.getOrElse(2) { "" }.split(',').filter { it.isNotEmpty() }.map { it.toLong() },
                )
            }.getOrDefault(Stopwatch())
        }
    }
}
