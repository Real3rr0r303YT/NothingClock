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

package com.real3rr0r303yt.nothingclock.ui.alarms

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingclock.R
import com.real3rr0r303yt.nothingclock.core.Alarm
import com.real3rr0r303yt.nothingclock.core.TimeText
import com.real3rr0r303yt.nothingclock.ui.ClockViewModel
import com.real3rr0r303yt.nothingclock.ui.components.CardContainer
import com.real3rr0r303yt.nothingclock.ui.components.DotMatrixText
import com.real3rr0r303yt.nothingclock.ui.components.EmptyState
import com.real3rr0r303yt.nothingclock.ui.components.nothingCardStroke
import com.real3rr0r303yt.nothingclock.ui.components.rememberNow
import com.real3rr0r303yt.nothingclock.ui.theme.NothingBlack
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGrey
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGreyDim
import com.real3rr0r303yt.nothingclock.ui.theme.NothingOutline
import com.real3rr0r303yt.nothingclock.ui.theme.NothingRed
import com.real3rr0r303yt.nothingclock.ui.theme.NothingWhite
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.time.temporal.WeekFields

@Composable
fun AlarmsScreen(alarms: List<Alarm>?, viewModel: ClockViewModel) {
    val context = LocalContext.current
    val is24h = DateFormat.is24HourFormat(context)
    var editing by remember { mutableStateOf<Alarm?>(null) }
    val setInFormat = stringResource(R.string.alarm_set_in)
    val announce: (Long?) -> Unit = { trigger ->
        if (trigger != null) {
            val (h, m) = TimeText.untilParts(trigger - System.currentTimeMillis())
            Toast.makeText(context, String.format(setInFormat, h, m), Toast.LENGTH_SHORT).show()
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            alarms == null -> Unit
            alarms.isEmpty() -> Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
                PermissionHints()
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(stringResource(R.string.alarms_empty), stringResource(R.string.alarms_empty_hint))
                }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "hints") { PermissionHints() }
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        is24h = is24h,
                        onClick = { editing = alarm },
                        onToggle = { enabled -> viewModel.setAlarmEnabled(alarm, enabled) { trigger -> if (enabled) announce(trigger) } },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val now = ZonedDateTime.now().plusHours(1)
                editing = viewModel.newAlarmDefaults(now.hour, 0)
            },
            containerColor = NothingRed,
            contentColor = NothingWhite,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(24.dp),
        ) {
            Icon(Icons.Outlined.Add, stringResource(R.string.action_add_alarm))
        }
    }

    editing?.let { alarm ->
        AlarmEditor(
            initial = alarm,
            onDismiss = { editing = null },
            onSave = {
                viewModel.saveAlarm(it.copy(enabled = true), announce)
                editing = null
            },
            onDelete = if (alarm.id != 0) {
                {
                    viewModel.deleteAlarm(alarm)
                    editing = null
                }
            } else {
                null
            },
        )
    }
}

@Composable
private fun AlarmCard(alarm: Alarm, is24h: Boolean, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    val nowMillis by rememberNow(60_000)
    val contentColor = if (alarm.enabled) NothingWhite else NothingGreyDim
    Card(
        shape = MaterialTheme.shapes.medium,
        border = nothingCardStroke(highlight = alarm.enabled),
        colors = CardDefaults.cardColors(containerColor = CardContainer),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                    DotMatrixText(TimeText.clock(alarm.hour, alarm.minute, is24h), dotSize = 4.dp, gap = 1.4.dp, monoDigits = true, color = contentColor)
                    if (!is24h) {
                        Spacer(Modifier.width(8.dp))
                        Text(if (alarm.hour < 12) "AM" else "PM", style = MaterialTheme.typography.labelSmall, color = NothingGrey)
                    }
                }
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NothingWhite,
                        checkedTrackColor = NothingRed,
                        checkedBorderColor = NothingRed,
                        uncheckedThumbColor = NothingGrey,
                        uncheckedTrackColor = NothingBlack,
                        uncheckedBorderColor = NothingOutline,
                    ),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DayDots(alarm.days)
                Spacer(Modifier.width(12.dp))
                Text(
                    listOf(alarm.label, repeatText(alarm.days)).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = NothingGrey,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
            if (alarm.enabled) {
                val now = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault())
                val (h, m) = TimeText.untilParts(alarm.nextTrigger(now).toInstant().toEpochMilli() - nowMillis)
                Text(
                    stringResource(R.string.alarms_next_in, h, m).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = NothingRed,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

/** Seven small letters for the week, selected days lit – starts on the locale's first day. */
@Composable
fun DayDots(days: Int, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        orderedWeek(locale).forEach { day ->
            val on = days and Alarm.dayBit(day) != 0
            Text(
                day.getDisplayName(TextStyle.NARROW, locale),
                style = MaterialTheme.typography.labelSmall,
                color = if (on) NothingWhite else NothingOutline,
            )
        }
    }
}

fun orderedWeek(locale: java.util.Locale): List<DayOfWeek> {
    val first = WeekFields.of(locale).firstDayOfWeek
    return (0 until 7).map { first.plus(it.toLong()) }
}

@Composable
fun repeatText(days: Int): String = when (days) {
    0 -> stringResource(R.string.alarms_once)
    Alarm.EVERY_DAY -> stringResource(R.string.alarms_every_day)
    Alarm.WEEKDAYS -> stringResource(R.string.alarms_weekdays)
    Alarm.WEEKEND -> stringResource(R.string.alarms_weekend)
    else -> ""
}
