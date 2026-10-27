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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.real3rr0r303yt.nothingclock.ui.components.SectionLabel
import com.real3rr0r303yt.nothingclock.ui.theme.NothingBlack
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGrey
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGreyDim
import com.real3rr0r303yt.nothingclock.ui.theme.NothingOutline
import com.real3rr0r303yt.nothingclock.ui.theme.NothingRed
import com.real3rr0r303yt.nothingclock.ui.theme.NothingSurface
import com.real3rr0r303yt.nothingclock.ui.theme.NothingSurfaceHigh
import com.real3rr0r303yt.nothingclock.ui.theme.NothingWhite
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditor(initial: Alarm, onDismiss: () -> Unit, onSave: (Alarm) -> Unit, onDelete: (() -> Unit)?) {
    val is24h = DateFormat.is24HourFormat(LocalContext.current)
    val locale = LocalConfiguration.current.locales[0]
    val time = rememberTimePickerState(initial.hour, initial.minute, is24h)
    var days by remember { mutableIntStateOf(initial.days) }
    var label by remember { mutableStateOf(initial.label) }
    var vibrate by remember { mutableStateOf(initial.vibrate) }
    var fade by remember { mutableIntStateOf(initial.fadeInSeconds) }
    var snooze by remember { mutableIntStateOf(initial.snoozeMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NothingSurface,
        titleContentColor = NothingWhite,
        title = { Text(stringResource(if (initial.id == 0) R.string.alarm_new else R.string.alarm_edit)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                TimeInput(
                    state = time,
                    colors = TimePickerDefaults.colors(
                        timeSelectorSelectedContainerColor = NothingRed,
                        timeSelectorSelectedContentColor = NothingWhite,
                        timeSelectorUnselectedContainerColor = NothingSurfaceHigh,
                        timeSelectorUnselectedContentColor = NothingWhite,
                        periodSelectorSelectedContainerColor = NothingRed,
                        periodSelectorSelectedContentColor = NothingWhite,
                        periodSelectorUnselectedContentColor = NothingGrey,
                        periodSelectorBorderColor = NothingOutline,
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                SectionLabel(stringResource(R.string.alarm_repeat), Modifier.padding(top = 0.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    orderedWeek(locale).forEach { day ->
                        val bit = Alarm.dayBit(day)
                        val on = days and bit != 0
                        TextButton(
                            onClick = { days = days xor bit },
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                containerColor = if (on) NothingRed else NothingBlack,
                                contentColor = if (on) NothingWhite else NothingGrey,
                            ),
                        ) {
                            Text(day.getDisplayName(TextStyle.NARROW, locale), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it.take(60) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.alarm_label)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        unfocusedBorderColor = NothingOutline,
                        cursorColor = NothingRed,
                        focusedLabelColor = NothingRed,
                        unfocusedLabelColor = NothingGreyDim,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                SectionLabel(stringResource(R.string.alarm_fade_in))
                ChoiceRow(listOf(0, 15, 30, 60), fade, { fade = it }) {
                    if (it == 0) stringResource(R.string.alarm_fade_off) else stringResource(R.string.alarm_fade_seconds, it)
                }
                SectionLabel(stringResource(R.string.alarm_snooze))
                ChoiceRow(listOf(5, 10, 15, 20), snooze, { snooze = it }) { stringResource(R.string.alarm_minutes, it) }

                Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.alarm_vibrate), color = NothingWhite, modifier = Modifier.weight(1f))
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it },
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
                if (onDelete != null) {
                    TextButton(onClick = onDelete, modifier = Modifier.padding(top = 8.dp)) {
                        Text(stringResource(R.string.alarm_delete).uppercase(), color = NothingRed)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    initial.copy(
                        hour = time.hour,
                        minute = time.minute,
                        days = days,
                        label = label.trim(),
                        vibrate = vibrate,
                        fadeInSeconds = fade,
                        snoozeMinutes = snooze,
                    ),
                )
            }) {
                Text(stringResource(R.string.alarm_save).uppercase(), color = NothingRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel).uppercase(), color = NothingGrey) }
        },
    )
}

@Composable
private fun ChoiceRow(options: List<Int>, selected: Int, onSelect: (Int) -> Unit, label: @Composable (Int) -> String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option), style = MaterialTheme.typography.labelSmall) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = NothingBlack,
                    labelColor = NothingGrey,
                    selectedContainerColor = NothingWhite,
                    selectedLabelColor = NothingBlack,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = option == selected,
                    borderColor = NothingOutline,
                    selectedBorderColor = NothingWhite,
                ),
            )
        }
    }
}
