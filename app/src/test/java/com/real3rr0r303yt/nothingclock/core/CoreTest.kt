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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

class CoreTest {

    private val berlin = ZoneId.of("Europe/Berlin")
    // Monday, 2026-10-26 08:00
    private val monday8 = ZonedDateTime.of(2026, 10, 26, 8, 0, 0, 0, berlin)

    @Test
    fun `one-shot alarm rings today or tomorrow`() {
        assertEquals(monday8.withHour(9), Alarm(1, 9, 0).nextTrigger(monday8))
        assertEquals(monday8.plusDays(1).withHour(7), Alarm(1, 7, 0).nextTrigger(monday8))
        // Exactly now -> next day, never "now".
        assertEquals(monday8.plusDays(1), Alarm(1, 8, 0).nextTrigger(monday8))
    }

    @Test
    fun `repeating alarm skips to the next selected day`() {
        val weekend = Alarm(1, 9, 30, days = Alarm.WEEKEND)
        val next = weekend.nextTrigger(monday8)
        assertEquals(DayOfWeek.SATURDAY, next.dayOfWeek)
        assertEquals(9, next.hour)
        assertEquals(30, next.minute)

        val weekdays = Alarm(1, 7, 0, days = Alarm.WEEKDAYS)
        assertEquals(DayOfWeek.TUESDAY, weekdays.nextTrigger(monday8).dayOfWeek)
        val friday8 = monday8.plusDays(4)
        assertEquals(DayOfWeek.MONDAY, weekdays.nextTrigger(friday8).dayOfWeek)
        assertTrue(weekdays.repeatsOn(DayOfWeek.FRIDAY))
        assertFalse(weekdays.repeatsOn(DayOfWeek.SUNDAY))
    }

    @Test
    fun `alarm inside a DST gap rings at the first valid instant`() {
        // 2027-03-28 02:30 does not exist in Berlin (clocks jump 02:00 -> 03:00).
        val saturday = ZonedDateTime.of(2027, 3, 27, 22, 0, 0, 0, berlin)
        val next = Alarm(1, 2, 30).nextTrigger(saturday)
        assertEquals(28, next.dayOfMonth)
        assertEquals(3, next.hour)
    }

    @Test
    fun `codec round-trips including special characters`() {
        val alarms = listOf(
            Alarm(1, 6, 45, Alarm.WEEKDAYS, label = "Früh; 100% wach\nheute", fadeInSeconds = 60, snoozeMinutes = 5),
            Alarm(2, 23, 0, enabled = false, vibrate = false),
        )
        assertEquals(alarms, AlarmCodec.decode(AlarmCodec.encode(alarms)))
        assertEquals(emptyList<Alarm>(), AlarmCodec.decode("garbage"))
        assertEquals(1, AlarmCodec.decode("v1\nbroken\n3;7;0;0;1;1;30;10;ok").size)
    }

    @Test
    fun `stopwatch accumulates across pauses and records laps`() {
        var s = Stopwatch().start(1_000)
        s = s.lap(3_000) // lap 1: 2 s
        s = s.pause(4_000)
        assertEquals(3_000, s.elapsed(99_999))
        s = s.start(10_000).lap(11_500) // lap 2: 2.5 s
        assertEquals(listOf(2_000L, 2_500L), s.lapDurations())
        assertEquals(s, Stopwatch.decode(Stopwatch.encode(s)))
        assertTrue(s.reset().isReset)
    }

    @Test
    fun `timer pauses, resumes and extends`() {
        var t = CountdownTimer().start(60_000, now = 0)
        assertEquals(30_000, t.remaining(30_000))
        t = t.pause(30_000)
        assertTrue(t.isPaused)
        t = t.resume(100_000)
        assertEquals(130_000L, t.endsAt)
        t = t.addTime(60_000, 100_000)
        assertEquals(90_000, t.remaining(100_000))
        assertEquals(0, t.remaining(1_000_000))
        assertEquals(t, CountdownTimer.decode(CountdownTimer.encode(t)))
    }

    @Test
    fun `keypad digits map to durations`() {
        assertEquals(90_000L, CountdownTimer.digitsToMillis("130"))
        assertEquals(90_000L, CountdownTimer.digitsToMillis("90"))
        assertEquals(3_723_000L, CountdownTimer.digitsToMillis("10203"))
        assertEquals("00:01:30", TimeText.keypad("130"))
    }

    @Test
    fun `time text formats`() {
        assertEquals("01:02.34", TimeText.stopwatch(62_345))
        assertEquals("1:00:00.00", TimeText.stopwatch(3_600_000))
        assertEquals("00:01", TimeText.countdown(1))
        assertEquals("1:00:00", TimeText.countdown(3_600_000))
        assertEquals("07:05", TimeText.clock(7, 5, true))
        assertEquals("12:00", TimeText.clock(0, 0, false))
        assertEquals("1:30", TimeText.clock(13, 30, false))
        assertEquals(1L to 1L, TimeText.untilParts(60 * 60_000L + 30_000))
    }

    @Test
    fun `alarm volume ramps up`() {
        assertEquals(0.05f, alarmVolume(0, 30_000), 0.001f)
        assertEquals(1f, alarmVolume(30_000, 30_000), 0.001f)
        assertEquals(1f, alarmVolume(5, 0), 0.001f)
        assertTrue(alarmVolume(10_000, 30_000) < alarmVolume(20_000, 30_000))
    }
}
