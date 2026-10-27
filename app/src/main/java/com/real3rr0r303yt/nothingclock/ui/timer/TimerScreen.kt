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

package com.real3rr0r303yt.nothingclock.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingclock.R
import com.real3rr0r303yt.nothingclock.core.CountdownTimer
import com.real3rr0r303yt.nothingclock.core.TimeText
import com.real3rr0r303yt.nothingclock.ui.ClockViewModel
import com.real3rr0r303yt.nothingclock.ui.alarms.PermissionHints
import com.real3rr0r303yt.nothingclock.ui.components.DotMatrixText
import com.real3rr0r303yt.nothingclock.ui.components.DotRing
import com.real3rr0r303yt.nothingclock.ui.components.SectionLabel
import com.real3rr0r303yt.nothingclock.ui.components.rememberWallFrameClock
import com.real3rr0r303yt.nothingclock.ui.theme.NothingBlack
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGrey
import com.real3rr0r303yt.nothingclock.ui.theme.NothingOutline
import com.real3rr0r303yt.nothingclock.ui.theme.NothingRed
import com.real3rr0r303yt.nothingclock.ui.theme.NothingWhite

private val PRESETS_MIN = listOf(1, 3, 5, 10, 15, 30)

@Composable
fun TimerScreen(timer: CountdownTimer, viewModel: ClockViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PermissionHints()
        if (timer.isIdle) TimerSetup(onStart = viewModel::timerStart) else RunningTimer(timer, viewModel)
    }
}

@Composable
private fun TimerSetup(onStart: (Long) -> Unit) {
    var digits by rememberSaveable { mutableStateOf("") }
    val duration = CountdownTimer.digitsToMillis(digits)

    Spacer(Modifier.height(24.dp))
    DotMatrixText(
        TimeText.keypad(digits),
        dotSize = 5.dp,
        gap = 1.6.dp,
        monoDigits = true,
        color = if (digits.isEmpty()) NothingGrey else NothingWhite,
        dimColor = NothingOutline.copy(alpha = 0.5f),
    )
    Spacer(Modifier.height(24.dp))

    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "00", "0", "⌫")
    keys.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            row.forEach { key ->
                TextButton(
                    onClick = {
                        digits = when (key) {
                            "⌫" -> digits.dropLast(1)
                            else -> (digits + key).trimStart('0').takeLast(6)
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(76.dp),
                ) {
                    if (key == "⌫") {
                        Icon(Icons.AutoMirrored.Outlined.Backspace, stringResource(R.string.timer_delete_digit), tint = NothingGrey)
                    } else {
                        Text(key, style = MaterialTheme.typography.headlineMedium, color = NothingWhite)
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    Button(
        onClick = { onStart(duration); digits = "" },
        enabled = duration > 0,
        colors = ButtonDefaults.buttonColors(containerColor = NothingRed, contentColor = NothingWhite, disabledContainerColor = NothingOutline),
        shape = CircleShape,
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Icon(Icons.Outlined.PlayArrow, null)
        Text(stringResource(R.string.timer_start).uppercase(), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 8.dp))
    }

    SectionLabel(stringResource(R.string.timer_presets), Modifier.fillMaxWidth())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PRESETS_MIN.forEach { minutes ->
            FilterChip(
                selected = false,
                onClick = { onStart(minutes * 60_000L) },
                label = { Text("$minutes′", style = MaterialTheme.typography.labelMedium) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(containerColor = NothingBlack, labelColor = NothingWhite),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = NothingOutline),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RunningTimer(timer: CountdownTimer, viewModel: ClockViewModel) {
    val now by rememberWallFrameClock(timer.isRunning)
    val remaining = timer.remaining(now)
    val progress = timer.progress(now)

    Box(Modifier.fillMaxWidth().aspectRatio(1f).padding(16.dp), contentAlignment = Alignment.Center) {
        DotRing(active = (progress * 60).toInt().coerceIn(0, 59), modifier = Modifier.fillMaxSize(), dotRadius = 3.dp)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DotMatrixText(
                TimeText.countdown(remaining),
                dotSize = 5.dp,
                gap = 1.8.dp,
                monoDigits = true,
                color = if (timer.isPaused) NothingGrey else NothingWhite,
                modifier = Modifier.padding(horizontal = 48.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(TimeText.countdown(timer.duration), style = MaterialTheme.typography.labelMedium, color = NothingGrey)
        }
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = viewModel::timerReset, shape = CircleShape, modifier = Modifier.weight(1f).height(56.dp)) {
            Text(stringResource(R.string.timer_reset).uppercase(), color = NothingWhite)
        }
        Button(
            onClick = { if (timer.isRunning) viewModel.timerPause() else viewModel.timerResume() },
            colors = ButtonDefaults.buttonColors(containerColor = NothingRed, contentColor = NothingWhite),
            shape = CircleShape,
            modifier = Modifier.weight(1f).height(56.dp),
        ) {
            Icon(if (timer.isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, null)
            Text(
                stringResource(if (timer.isRunning) R.string.timer_pause else R.string.timer_resume).uppercase(),
                modifier = Modifier.padding(start = 6.dp),
            )
        }
        OutlinedButton(onClick = viewModel::timerAddMinute, shape = CircleShape, modifier = Modifier.weight(1f).height(56.dp)) {
            Text(stringResource(R.string.action_plus_minute), color = NothingWhite)
        }
    }
}
