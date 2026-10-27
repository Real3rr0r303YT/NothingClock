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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.real3rr0r303yt.nothingclock.MainActivity
import com.real3rr0r303yt.nothingclock.core.Alarm
import java.time.ZonedDateTime

/**
 * Schedules alarms with AlarmManager.setAlarmClock(): exact, allowed in Doze, and shown
 * as the system's "next alarm". Timers use setExactAndAllowWhileIdle().
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** On Android 12 (API 31/32) the user may revoke exact alarms; 33+ uses USE_EXACT_ALARM. */
    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    /** Schedules the next occurrence of [alarm] (or cancels it when disabled). Returns trigger millis. */
    fun schedule(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now()): Long? {
        cancel(alarm.id)
        if (!alarm.enabled) return null
        val trigger = alarm.nextTrigger(now).toInstant().toEpochMilli()
        setAlarmClock(trigger, ringIntent(alarm, snoozed = false, requestCode = alarm.id))
        return trigger
    }

    fun snooze(alarm: Alarm, minutes: Int) {
        val trigger = System.currentTimeMillis() + minutes * 60_000L
        setAlarmClock(trigger, ringIntent(alarm, snoozed = true, requestCode = SNOOZE_BASE + alarm.id))
    }

    fun cancel(alarmId: Int) {
        alarmManager.cancel(ringIntent(Alarm(alarmId, 0, 0), snoozed = false, requestCode = alarmId))
        alarmManager.cancel(ringIntent(Alarm(alarmId, 0, 0), snoozed = true, requestCode = SNOOZE_BASE + alarmId))
    }

    fun rescheduleAll(alarms: List<Alarm>) = alarms.forEach { schedule(it) }

    fun scheduleTimer(endsAt: Long) {
        val pi = timerIntent()
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endsAt, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endsAt, pi)
        }
    }

    fun cancelTimer() = alarmManager.cancel(timerIntent())

    private fun setAlarmClock(trigger: Long, operation: PendingIntent) {
        val show = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_ALARMS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (canScheduleExact()) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, show), operation)
        } else {
            // Degraded mode until the user grants exact alarms again.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, operation)
        }
    }

    private fun ringIntent(alarm: Alarm, snoozed: Boolean, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(AlarmReceiver.ACTION_ALARM)
            // Distinct data per alarm so PendingIntents never collide.
            .setData(android.net.Uri.parse("nothingclock://alarm/$requestCode"))
            .putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            .putExtra(AlarmReceiver.EXTRA_SNOOZED, snoozed)
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun timerIntent(): PendingIntent = PendingIntent.getBroadcast(
        context, TIMER_REQUEST,
        Intent(context, AlarmReceiver::class.java).setAction(AlarmReceiver.ACTION_TIMER),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private companion object {
        const val SNOOZE_BASE = 100_000
        const val TIMER_REQUEST = 900_000
    }
}
