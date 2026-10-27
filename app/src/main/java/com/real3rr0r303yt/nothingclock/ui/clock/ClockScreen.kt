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

package com.real3rr0r303yt.nothingclock.ui.clock

import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingclock.R
import com.real3rr0r303yt.nothingclock.core.Alarm
import com.real3rr0r303yt.nothingclock.core.TimeText
import com.real3rr0r303yt.nothingclock.ui.components.CardContainer
import com.real3rr0r303yt.nothingclock.ui.components.DotMatrixText
import com.real3rr0r303yt.nothingclock.ui.components.DotRing
import com.real3rr0r303yt.nothingclock.ui.components.SectionLabel
import com.real3rr0r303yt.nothingclock.ui.components.nothingCardStroke
import com.real3rr0r303yt.nothingclock.ui.components.rememberNow
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGrey
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGreyDim
import com.real3rr0r303yt.nothingclock.ui.theme.NothingOutline
import com.real3rr0r303yt.nothingclock.ui.theme.NothingRed
import com.real3rr0r303yt.nothingclock.ui.theme.NothingSurface
import com.real3rr0r303yt.nothingclock.ui.theme.NothingWhite
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClockScreen(alarms: List<Alarm>, zones: List<String>, onAddZone: (String) -> Unit, onRemoveZone: (String) -> Unit) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val is24h = DateFormat.is24HourFormat(context)
    val nowMillis by rememberNow(1_000)
    val now = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault())
    var picking by remember { mutableStateOf(false) }
    var removing by remember { mutableStateOf<String?>(null) }

    val nextAlarm = alarms.filter { it.enabled }.minOfOrNull { it.nextTrigger(now).toInstant().toEpochMilli() }

    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp), modifier = Modifier.fillMaxSize()) {
        item(key = "face") {
            Box(Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp), contentAlignment = Alignment.Center) {
                DotRing(active = now.second, modifier = Modifier.fillMaxSize())
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DotMatrixText(
                        TimeText.clock(now.hour, now.minute, is24h),
                        dotSize = 6.dp,
                        gap = 2.dp,
                        monoDigits = true,
                        dimColor = NothingOutline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 40.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    DotMatrixText("%02d".format(now.second), dotSize = 2.4.dp, gap = 1.dp, monoDigits = true, color = NothingRed)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)).uppercase(locale),
                        style = MaterialTheme.typography.labelMedium,
                        color = NothingGrey,
                    )
                    if (!is24h) {
                        Text(if (now.hour < 12) "AM" else "PM", style = MaterialTheme.typography.labelSmall, color = NothingGreyDim)
                    }
                }
            }
        }
        if (nextAlarm != null) {
            item(key = "next") {
                val next = Instant.ofEpochMilli(nextAlarm).atZone(ZoneId.systemDefault())
                val text = next.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale))
                Text(
                    stringResource(R.string.clock_next_alarm, text).uppercase(locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = NothingGrey,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        item(key = "world_label") { SectionLabel(stringResource(R.string.clock_world)) }
        items(zones, key = { it }) { zoneId ->
            val zone = runCatching { ZoneId.of(zoneId) }.getOrNull() ?: return@items
            val there = now.withZoneSameInstant(zone)
            Card(
                shape = MaterialTheme.shapes.medium,
                border = nothingCardStroke(),
                colors = CardDefaults.cardColors(containerColor = CardContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .combinedClickable(onClick = {}, onLongClick = { removing = zoneId }),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(cityName(zoneId), style = MaterialTheme.typography.titleMedium, color = NothingWhite)
                        Text(offsetText(now, there), style = MaterialTheme.typography.labelSmall, color = NothingGrey)
                    }
                    DotMatrixText(TimeText.clock(there.hour, there.minute, is24h), dotSize = 2.6.dp, gap = 1.dp, monoDigits = true)
                }
            }
        }
        item(key = "add") {
            OutlinedButton(onClick = { picking = true }, shape = CircleShape, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Add, null, tint = NothingWhite)
                Text(stringResource(R.string.clock_add_city), color = NothingWhite, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }

    if (picking) {
        ZonePicker(exclude = zones.toSet(), onPick = { onAddZone(it); picking = false }, onDismiss = { picking = false })
    }
    removing?.let { zoneId ->
        AlertDialog(
            onDismissRequest = { removing = null },
            containerColor = NothingSurface,
            title = { Text(cityName(zoneId), color = NothingWhite) },
            confirmButton = {
                TextButton(onClick = { onRemoveZone(zoneId); removing = null }) {
                    Text(stringResource(R.string.clock_remove_city).uppercase(), color = NothingRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { removing = null }) { Text(stringResource(R.string.action_cancel).uppercase(), color = NothingGrey) }
            },
        )
    }
}

fun cityName(zoneId: String): String = zoneId.substringAfterLast('/').replace('_', ' ')

@Composable
private fun offsetText(here: ZonedDateTime, there: ZonedDateTime): String {
    val diffMinutes = (there.offset.totalSeconds - here.offset.totalSeconds) / 60
    if (diffMinutes == 0) return stringResource(R.string.clock_same_time)
    val h = abs(diffMinutes) / 60
    val m = abs(diffMinutes) % 60
    val amount = if (m == 0) "$h h" else "$h h $m min"
    return stringResource(if (diffMinutes > 0) R.string.clock_offset_ahead else R.string.clock_offset_behind, amount)
}

@Composable
private fun ZonePicker(exclude: Set<String>, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val all = remember {
        ZoneId.getAvailableZoneIds()
            .filter { '/' in it && !it.startsWith("Etc/") && !it.startsWith("SystemV/") }
            .sortedBy { cityName(it) }
    }
    var query by remember { mutableStateOf("") }
    val shown = remember(query) {
        all.filter { it !in exclude && (query.isBlank() || it.contains(query.trim().replace(' ', '_'), ignoreCase = true)) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NothingSurface,
        title = { Text(stringResource(R.string.clock_add_city), color = NothingWhite) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.clock_search_city), color = NothingGreyDim) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NothingRed, unfocusedBorderColor = NothingOutline, cursorColor = NothingRed),
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(Modifier.heightIn(max = 360.dp).padding(top = 8.dp)) {
                    items(shown, key = { it }) { id ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPick(id) }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(cityName(id), color = NothingWhite)
                            Text(id.substringBefore('/'), style = MaterialTheme.typography.labelSmall, color = NothingGreyDim)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel).uppercase(), color = NothingGrey) }
        },
    )
}
