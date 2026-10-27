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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.real3rr0r303yt.nothingclock.R

object Notifications {

    const val CHANNEL_ALARMS = "alarms"
    const val CHANNEL_TIMERS = "timers"
    const val ID_RINGING = 1

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        listOf(
            NotificationChannel(CHANNEL_ALARMS, context.getString(R.string.channel_alarms), NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_TIMERS, context.getString(R.string.channel_timers), NotificationManager.IMPORTANCE_HIGH),
        ).forEach { channel ->
            // The service plays sound and vibration itself (with fade-in), so the channel stays silent.
            channel.setSound(null, null)
            channel.enableVibration(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            manager.createNotificationChannel(channel)
        }
    }

    fun ringing(context: Context, ring: RingService.Ringing): Notification {
        val fullScreen = PendingIntent.getActivity(
            context, 1,
            Intent(context, RingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val isAlarm = ring.kind == RingService.Kind.ALARM
        val builder = NotificationCompat.Builder(context, if (isAlarm) CHANNEL_ALARMS else CHANNEL_TIMERS)
            .setSmallIcon(R.drawable.ic_stat_clock)
            .setContentTitle(ring.title.ifBlank { context.getString(if (isAlarm) R.string.ring_alarm else R.string.ring_timer) })
            .setContentText(context.getString(if (isAlarm) R.string.ring_alarm_text else R.string.ring_timer_text))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setColor(0xFFDA291C.toInt())
            .setContentIntent(fullScreen)
            .setFullScreenIntent(fullScreen, true)
        if (isAlarm) {
            builder.addAction(0, context.getString(R.string.action_snooze), serviceAction(context, RingService.ACTION_SNOOZE, 2))
        } else {
            builder.addAction(0, context.getString(R.string.action_plus_minute), serviceAction(context, RingService.ACTION_PLUS_MINUTE, 4))
        }
        builder.addAction(0, context.getString(if (isAlarm) R.string.action_dismiss else R.string.action_stop), serviceAction(context, RingService.ACTION_DISMISS, 3))
        return builder.build()
    }

    private fun serviceAction(context: Context, action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            context, requestCode,
            Intent(context, RingService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
