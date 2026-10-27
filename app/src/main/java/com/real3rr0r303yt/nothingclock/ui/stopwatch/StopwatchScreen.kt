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

package com.real3rr0r303yt.nothingclock.ui.stopwatch

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingclock.R
import com.real3rr0r303yt.nothingclock.core.Stopwatch
import com.real3rr0r303yt.nothingclock.core.TimeText
import com.real3rr0r303yt.nothingclock.ui.ClockViewModel
import com.real3rr0r303yt.nothingclock.ui.components.DotMatrixText
import com.real3rr0r303yt.nothingclock.ui.components.DotRing
import com.real3rr0r303yt.nothingclock.ui.components.RedDot
import com.real3rr0r303yt.nothingclock.ui.components.rememberFrameClock
import com.real3rr0r303yt.nothingclock.ui.theme.MonoFamily
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGrey
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGreyDim
import com.real3rr0r303yt.nothingclock.ui.theme.NothingOutline
import com.real3rr0r303yt.nothingclock.ui.theme.NothingRed
import com.real3rr0r303yt.nothingclock.ui.theme.NothingWhite

@Composable
fun StopwatchScreen(stopwatch: Stopwatch, viewModel: ClockViewModel) {
    val now by rememberFrameClock(stopwatch.isRunning)
    val elapsed = stopwatch.elapsed(now)
    val laps = remember(stopwatch.laps) { stopwatch.lapDurations() }
    val best = if (laps.size >= 2) laps.indexOf(laps.min()) else -1
    val worst = if (laps.size >= 2) laps.indexOf(laps.max()) else -1

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "dial") {
            Box(Modifier.fillMaxWidth().aspectRatio(1.1f).padding(16.dp), contentAlignment = Alignment.Center) {
                DotRing(active = ((elapsed / 1000) % 60).toInt(), modifier = Modifier.fillMaxSize())
                DotMatrixText(
                    TimeText.stopwatch(elapsed),
                    dotSize = 4.dp,
                    gap = 1.4.dp,
                    monoDigits = true,
                    color = if (stopwatch.isRunning || stopwatch.isReset) NothingWhite else NothingGrey,
                    modifier = Modifier.padding(horizontal = 40.dp),
                )
            }
        }
        item(key = "buttons") {
            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { if (stopwatch.isRunning) viewModel.stopwatchLap() else viewModel.stopwatchReset() },
                    enabled = !stopwatch.isReset,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) {
                    Text(
                        stringResource(if (stopwatch.isRunning) R.string.stopwatch_lap else R.string.stopwatch_reset).uppercase(),
                        color = if (stopwatch.isReset) NothingGreyDim else NothingWhite,
                    )
                }
                Button(
                    onClick = { if (stopwatch.isRunning) viewModel.stopwatchPause() else viewModel.stopwatchStart() },
                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed, contentColor = NothingWhite),
                    shape = CircleShape,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) {
                    Text(stringResource(if (stopwatch.isRunning) R.string.stopwatch_pause else R.string.stopwatch_start).uppercase())
                }
            }
        }
        // Newest lap first.
        items(laps.indices.reversed().toList(), key = { it }) { i ->
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.stopwatch_lap_number, i + 1),
                    style = MaterialTheme.typography.labelMedium,
                    color = NothingGrey,
                    modifier = Modifier.width(88.dp),
                )
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    when (i) {
                        best -> Tag(stringResource(R.string.stopwatch_best), NothingWhite)
                        worst -> Tag(stringResource(R.string.stopwatch_worst), NothingRed)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        TimeText.stopwatch(laps[i]),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = MonoFamily),
                        color = if (i == worst) NothingRed else NothingWhite,
                    )
                    Text(TimeText.stopwatch(stopwatch.laps[i]), style = MaterialTheme.typography.labelSmall, color = NothingGreyDim)
                }
            }
            HorizontalDivider(color = NothingOutline)
        }
    }
}

@Composable
private fun Tag(text: String, color: Color) {
    RedDot(size = 5.dp, color = color)
    Spacer(Modifier.width(6.dp))
    Text(text, style = MaterialTheme.typography.labelSmall, color = color)
}
