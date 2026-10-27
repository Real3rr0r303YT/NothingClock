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

/** Formatting helpers for the dot-matrix displays (locale independent digits). */
object TimeText {

    /** Stopwatch: "MM:SS.cc", or "H:MM:SS.cc" once past an hour. */
    fun stopwatch(ms: Long): String {
        val total = ms.coerceAtLeast(0)
        val centis = (total / 10) % 100
        val seconds = (total / 1000) % 60
        val minutes = (total / 60_000) % 60
        val hours = total / 3_600_000
        return if (hours > 0) "%d:%02d:%02d.%02d".format(hours, minutes, seconds, centis)
        else "%02d:%02d.%02d".format(minutes, seconds, centis)
    }

    /** Timer: "MM:SS" or "H:MM:SS"; rounds up so it shows 00:01 until the very end. */
    fun countdown(ms: Long): String {
        val totalSeconds = (ms.coerceAtLeast(0) + 999) / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds / 60) % 60
        val s = totalSeconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    /** Keypad entry shown as "HH:MM:SS". */
    fun keypad(digits: String): String {
        val d = digits.takeLast(6).padStart(6, '0')
        return "${d.substring(0, 2)}:${d.substring(2, 4)}:${d.substring(4, 6)}"
    }

    /** Clock face, e.g. "07:05" (24 h) or "7:05" (12 h). */
    fun clock(hour: Int, minute: Int, is24h: Boolean): String =
        if (is24h) "%02d:%02d".format(hour, minute)
        else "%d:%02d".format(((hour + 11) % 12) + 1, minute)

    /** Duration until an alarm, split into (hours, minutes) rounded up to the next minute. */
    fun untilParts(ms: Long): Pair<Long, Long> {
        val minutes = (ms.coerceAtLeast(0) + 59_999) / 60_000
        return minutes / 60 to minutes % 60
    }
}

/** Volume ramp for alarms: quadratic ease-in from 5 % to 100 % over [fadeInMs]. */
fun alarmVolume(elapsedMs: Long, fadeInMs: Long): Float {
    if (fadeInMs <= 0) return 1f
    val t = (elapsedMs.toFloat() / fadeInMs).coerceIn(0f, 1f)
    return 0.05f + 0.95f * t * t
}
