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

import java.time.DayOfWeek
import java.time.ZonedDateTime

/**
 * A stored alarm. [days] is a bitmask with bit 0 = Monday … bit 6 = Sunday;
 * 0 means "ring once".
 */
data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val days: Int = 0,
    val label: String = "",
    val enabled: Boolean = true,
    val vibrate: Boolean = true,
    /** Seconds to fade the volume in; 0 = full volume immediately. */
    val fadeInSeconds: Int = 30,
    val snoozeMinutes: Int = 10,
) {
    init {
        require(hour in 0..23 && minute in 0..59) { "Invalid time $hour:$minute" }
        require(days in 0..0x7F) { "Invalid days mask $days" }
    }

    val repeats: Boolean get() = days != 0

    fun repeatsOn(day: DayOfWeek): Boolean = days and dayBit(day) != 0

    /** Next moment this alarm should ring strictly after [now]. */
    fun nextTrigger(now: ZonedDateTime): ZonedDateTime {
        var candidate = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        // DST gaps: withHour may shift forward; fine – the alarm rings at the first valid instant.
        if (!candidate.isAfter(now)) candidate = candidate.plusDays(1).withHour(hour).withMinute(minute)
        if (!repeats) return candidate
        repeat(8) {
            if (repeatsOn(candidate.dayOfWeek)) return candidate
            candidate = candidate.plusDays(1).withHour(hour).withMinute(minute)
        }
        error("unreachable: days mask $days")
    }

    companion object {
        const val WEEKDAYS = 0b0011111
        const val WEEKEND = 0b1100000
        const val EVERY_DAY = 0b1111111

        fun dayBit(day: DayOfWeek): Int = 1 shl (day.value - 1)
    }
}

/**
 * Compact, versioned text encoding for the alarm list (stored in DataStore).
 * One alarm per line, fields separated by ';', label percent-encoded.
 */
object AlarmCodec {
    private const val VERSION = "v1"

    fun encode(alarms: List<Alarm>): String = buildString {
        append(VERSION)
        alarms.forEach { a ->
            append('\n')
            append(listOf(a.id, a.hour, a.minute, a.days, if (a.enabled) 1 else 0, if (a.vibrate) 1 else 0, a.fadeInSeconds, a.snoozeMinutes).joinToString(";"))
            append(';').append(escape(a.label))
        }
    }

    /** Lenient: malformed lines are skipped instead of losing every alarm. */
    fun decode(text: String?): List<Alarm> {
        if (text.isNullOrBlank()) return emptyList()
        val lines = text.split('\n')
        if (lines.first() != VERSION) return emptyList()
        return lines.drop(1).mapNotNull { line ->
            runCatching {
                val f = line.split(';', limit = 9)
                Alarm(
                    id = f[0].toInt(),
                    hour = f[1].toInt(),
                    minute = f[2].toInt(),
                    days = f[3].toInt(),
                    enabled = f[4] == "1",
                    vibrate = f[5] == "1",
                    fadeInSeconds = f[6].toInt().coerceIn(0, 300),
                    snoozeMinutes = f[7].toInt().coerceIn(1, 60),
                    label = unescape(f.getOrElse(8) { "" }),
                )
            }.getOrNull()
        }
    }

    private fun escape(s: String) = buildString {
        s.forEach { c -> if (c == '%' || c == ';' || c == '\n' || c == '\r') append("%%%02X".format(c.code)) else append(c) }
    }

    private fun unescape(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            if (s[i] == '%' && i + 2 < s.length) {
                val code = s.substring(i + 1, i + 3).toIntOrNull(16)
                if (code != null) {
                    sb.append(code.toChar())
                    i += 3
                    continue
                }
            }
            sb.append(s[i])
            i++
        }
        return sb.toString()
    }
}
