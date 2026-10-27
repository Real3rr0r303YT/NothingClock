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

package com.real3rr0r303yt.nothingclock.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.real3rr0r303yt.nothingclock.R
import com.real3rr0r303yt.nothingclock.ui.alarms.AlarmsScreen
import com.real3rr0r303yt.nothingclock.ui.clock.ClockScreen
import com.real3rr0r303yt.nothingclock.ui.components.NothingIconButton
import com.real3rr0r303yt.nothingclock.ui.components.NothingTopBar
import com.real3rr0r303yt.nothingclock.ui.components.RedDot
import com.real3rr0r303yt.nothingclock.ui.components.dotGridBackground
import com.real3rr0r303yt.nothingclock.ui.settings.SettingsScreen
import com.real3rr0r303yt.nothingclock.ui.stopwatch.StopwatchScreen
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGreyDim
import com.real3rr0r303yt.nothingclock.ui.theme.NothingOutline
import com.real3rr0r303yt.nothingclock.ui.theme.NothingRed
import com.real3rr0r303yt.nothingclock.ui.theme.NothingWhite
import com.real3rr0r303yt.nothingclock.ui.timer.TimerScreen

private enum class Tab(val label: Int, val title: Int, val icon: ImageVector) {
    CLOCK(R.string.tab_clock, R.string.title_clock, Icons.Outlined.AccessTime),
    ALARMS(R.string.tab_alarms, R.string.title_alarms, Icons.Outlined.Alarm),
    TIMER(R.string.tab_timer, R.string.title_timer, Icons.Outlined.HourglassEmpty),
    STOPWATCH(R.string.tab_stopwatch, R.string.title_stopwatch, Icons.Outlined.Timer),
}

@Composable
fun ClockApp(requestedTab: Int, onTabConsumed: () -> Unit, viewModel: ClockViewModel = viewModel()) {
    var tab by rememberSaveable { mutableStateOf(Tab.CLOCK) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(requestedTab) {
        Tab.entries.getOrNull(requestedTab)?.let {
            tab = it
            showSettings = false
        }
        if (requestedTab >= 0) onTabConsumed()
    }
    BackHandler(enabled = showSettings) { showSettings = false }

    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val stopwatch by viewModel.stopwatch.collectAsStateWithLifecycle()
    val zones by viewModel.zones.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().dotGridBackground()) {
        if (showSettings) {
            NothingTopBar(title = stringResource(R.string.title_settings), onBack = { showSettings = false })
            SettingsScreen(settings) { transform -> viewModel.updateSettings(transform) }
            return@Column
        }

        NothingTopBar(title = stringResource(tab.title)) {
            NothingIconButton(Icons.Outlined.Settings, stringResource(R.string.action_settings), { showSettings = true })
        }
        Box(Modifier.weight(1f)) {
            when (tab) {
                Tab.CLOCK -> ClockScreen(
                    alarms = alarms.orEmpty(),
                    zones = zones,
                    onAddZone = { viewModel.addZone(it) },
                    onRemoveZone = { viewModel.removeZone(it) },
                )
                Tab.ALARMS -> AlarmsScreen(alarms, viewModel)
                Tab.TIMER -> TimerScreen(timer, viewModel)
                Tab.STOPWATCH -> StopwatchScreen(stopwatch, viewModel)
            }
        }
        BottomBar(tab, timerRunning = timer.isRunning, stopwatchRunning = stopwatch.isRunning) { tab = it }
    }
}

@Composable
private fun BottomBar(selected: Tab, timerRunning: Boolean, stopwatchRunning: Boolean, onSelect: (Tab) -> Unit) {
    Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
        HorizontalDivider(color = NothingOutline)
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Tab.entries.forEach { t ->
                val active = t == selected
                val running = (t == Tab.TIMER && timerRunning) || (t == Tab.STOPWATCH && stopwatchRunning)
                Column(
                    Modifier
                        .clickable { onSelect(t) }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(t.icon, null, tint = if (active) NothingWhite else NothingGreyDim)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(t.label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) NothingWhite else NothingGreyDim,
                    )
                    Spacer(Modifier.height(4.dp))
                    // Red dot: selected tab, or a timer/stopwatch running in the background.
                    if (active || running) RedDot(size = 4.dp, color = if (active) NothingRed else NothingRed.copy(alpha = 0.6f))
                    else Spacer(Modifier.size(4.dp))
                }
            }
        }
    }
}
