/*
 * NothingClock – private clock, alarm, timer & stopwatch for Android
 * Copyright (C) 2026 Real3rr0r303YT – Made by 3rr0r303YT on TikTok
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

package com.real3rr0r303yt.nothingclock.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.real3rr0r303yt.nothingclock.MainActivity
import com.real3rr0r303yt.nothingclock.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Dot-matrix clock: time (red colon), date and the next alarm of any app.
 *
 * Battery friendly: the minute tick is a *non-wakeup* RTC alarm – while the screen is off
 * nothing runs; the pending tick is delivered as soon as the phone wakes up.
 */
class ClockWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        render(context, manager, ids)
        scheduleTick(context)
    }

    override fun onEnabled(context: Context) = scheduleTick(context)

    override fun onDisabled(context: Context) = cancelTick(context)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED,
            -> updateAll(context)
        }
    }

    companion object {
        private const val ACTION_TICK = "com.real3rr0r303yt.nothingclock.widget.TICK"

        /** Redraws every clock widget (also called when alarms change in the app). */
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ClockWidget::class.java))
            if (ids.isEmpty()) {
                cancelTick(context)
                return
            }
            render(context, manager, ids)
            scheduleTick(context)
        }

        private fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val density = context.resources.displayMetrics.density
            val locale = context.resources.configuration.locales[0]
            val now = LocalDateTime.now()
            val is24 = DateFormat.is24HourFormat(context)
            val hour = if (is24) now.hour else (now.hour % 12).let { if (it == 0) 12 else it }
            val time = if (is24) "%02d:%02d".format(hour, now.minute) else "%d:%02d".format(hour, now.minute)
            val date = DateTimeFormatter.ofPattern("EEE d MMM", locale).format(now)
                .replace(".", "").uppercase(locale)

            val views = RemoteViews(context.packageName, R.layout.widget_clock)
            views.setImageViewBitmap(
                R.id.widget_time,
                DotRender.text(time, 9f * density) { _, c -> if (c == ':') DotRender.RED else DotRender.WHITE },
            )
            views.setContentDescription(R.id.widget_time, time)
            views.setImageViewBitmap(R.id.widget_date, DotRender.text(date, 3f * density) { _, _ -> DotRender.GREY })
            views.setContentDescription(R.id.widget_date, date)

            val next = context.getSystemService(AlarmManager::class.java)?.nextAlarmClock
            if (next != null) {
                val at = LocalDateTime.ofInstant(Instant.ofEpochMilli(next.triggerTime), ZoneId.systemDefault())
                val pattern = if (is24) "HH:mm" else "h:mm"
                val day = if (at.toLocalDate() == now.toLocalDate()) "" else
                    DateTimeFormatter.ofPattern("EEE ", locale).format(at).replace(".", "").uppercase(locale)
                val text = "• " + day + DateTimeFormatter.ofPattern(pattern, locale).format(at)
                views.setImageViewBitmap(R.id.widget_alarm, DotRender.text(text, 3f * density) { _, _ -> DotRender.RED })
                views.setContentDescription(R.id.widget_alarm, text)
                views.setViewVisibility(R.id.widget_alarm, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_alarm, View.GONE)
            }

            val open = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            views.setOnClickPendingIntent(R.id.widget_root, open)
            manager.updateAppWidget(ids, views)
        }

        private fun tickIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, ClockWidget::class.java).setAction(ACTION_TICK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        private fun scheduleTick(context: Context) {
            val alarms = context.getSystemService(AlarmManager::class.java) ?: return
            val next = (System.currentTimeMillis() / 60_000L + 1) * 60_000L
            val pi = tickIntent(context)
            // RTC (not RTC_WAKEUP): never wakes the phone just to redraw the clock.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarms.canScheduleExactAlarms()) {
                alarms.setWindow(AlarmManager.RTC, next, 5_000L, pi)
            } else {
                alarms.setExact(AlarmManager.RTC, next, pi)
            }
        }

        private fun cancelTick(context: Context) {
            context.getSystemService(AlarmManager::class.java)?.cancel(tickIntent(context))
        }
    }
}
