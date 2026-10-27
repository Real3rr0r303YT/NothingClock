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
 * Countdown timer state. While running, [endsAt] is a wall-clock epoch-millis deadline
 * (scheduled with AlarmManager); while paused, [remaining] holds what is left.
 */
data class CountdownTimer(
    val duration: Long = 0,
    val remaining: Long = 0,
    val endsAt: Long? = null,
) {
    val isRunning: Boolean get() = endsAt != null
    val isIdle: Boolean get() = !isRunning && remaining == 0L
    val isPaused: Boolean get() = !isRunning && remaining > 0

    fun remaining(now: Long): Long = endsAt?.let { (it - now).coerceAtLeast(0) } ?: remaining

    fun start(durationMs: Long, now: Long): CountdownTimer {
        require(durationMs > 0) { "Duration must be positive" }
        return CountdownTimer(duration = durationMs, remaining = durationMs, endsAt = now + durationMs)
    }

    fun pause(now: Long) = if (!isRunning) this else copy(remaining = remaining(now), endsAt = null)

    fun resume(now: Long) = if (!isPaused) this else copy(endsAt = now + remaining)

    fun addTime(ms: Long, now: Long): CountdownTimer = when {
        isRunning -> copy(duration = duration + ms, endsAt = endsAt!! + ms)
        isPaused -> copy(duration = duration + ms, remaining = remaining + ms)
        else -> this
    }

    fun reset() = CountdownTimer()

    /** 0..1 progress of the elapsed part. */
    fun progress(now: Long): Float = if (duration <= 0) 0f else (1f - remaining(now).toFloat() / duration).coerceIn(0f, 1f)

    companion object {
        fun encode(t: CountdownTimer) = "${t.duration};${t.remaining};${t.endsAt ?: ""}"

        fun decode(text: String?): CountdownTimer {
            if (text.isNullOrBlank()) return CountdownTimer()
            return runCatching {
                val f = text.split(';')
                CountdownTimer(f[0].toLong(), f[1].toLong(), f[2].takeIf { it.isNotEmpty() }?.toLong())
            }.getOrDefault(CountdownTimer())
        }

        /**
         * Keypad entry like a microwave: up to six digits fill HHMMSS from the right.
         * "130" → 1 min 30 s; "90" → 90 s.
         */
        fun digitsToMillis(digits: String): Long {
            val d = digits.filter { it.isDigit() }.takeLast(6).padStart(6, '0')
            val h = d.substring(0, 2).toLong()
            val m = d.substring(2, 4).toLong()
            val s = d.substring(4, 6).toLong()
            return ((h * 60 + m) * 60 + s) * 1000
        }
    }
}
