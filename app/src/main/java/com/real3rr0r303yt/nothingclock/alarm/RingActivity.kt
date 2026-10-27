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

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingclock.R
import com.real3rr0r303yt.nothingclock.core.TimeText
import com.real3rr0r303yt.nothingclock.ui.components.DotMatrixText
import com.real3rr0r303yt.nothingclock.ui.components.RedDot
import com.real3rr0r303yt.nothingclock.ui.components.dotGridBackground
import com.real3rr0r303yt.nothingclock.ui.components.rememberNow
import com.real3rr0r303yt.nothingclock.ui.theme.NothingClockTheme
import com.real3rr0r303yt.nothingclock.ui.theme.NothingBlack
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGrey
import com.real3rr0r303yt.nothingclock.ui.theme.NothingOutline
import com.real3rr0r303yt.nothingclock.ui.theme.NothingRed
import com.real3rr0r303yt.nothingclock.ui.theme.NothingWhite
import java.time.Instant
import java.time.ZoneId

/** Full-screen ringing UI, shown over the lock screen. */
class RingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(SystemBarStyle.dark(Color.BLACK), SystemBarStyle.dark(Color.BLACK))
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val is24h = DateFormat.is24HourFormat(this)

        setContent {
            NothingClockTheme {
                val ring by RingService.state.collectAsState()
                LaunchedEffect(ring) { if (ring == null) finish() }
                val now by rememberNow(1_000)
                val time = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalTime()
                val current = ring ?: return@NothingClockTheme
                val isAlarm = current.kind == RingService.Kind.ALARM

                Column(
                    Modifier
                        .fillMaxSize()
                        .dotGridBackground()
                        .systemBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RedDot(size = 8.dp)
                        Spacer(Modifier.padding(4.dp))
                        Text(
                            stringResource(if (isAlarm) R.string.ring_alarm else R.string.ring_timer).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = NothingGrey,
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                    DotMatrixText(TimeText.clock(time.hour, time.minute, is24h), dotSize = 9.dp, gap = 3.dp, monoDigits = true, dimColor = NothingOutline)
                    if (current.title.isNotBlank()) {
                        Spacer(Modifier.height(24.dp))
                        Text(current.title, style = MaterialTheme.typography.headlineSmall, color = NothingWhite)
                    }
                    Spacer(Modifier.height(64.dp))
                    Button(
                        onClick = {
                            startService(RingService.command(this@RingActivity, if (isAlarm) RingService.ACTION_SNOOZE else RingService.ACTION_PLUS_MINUTE))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NothingWhite, contentColor = NothingBlack),
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                    ) {
                        Text(
                            if (isAlarm) stringResource(R.string.action_snooze_minutes, current.alarm?.snoozeMinutes ?: 10).uppercase()
                            else stringResource(R.string.action_plus_minute).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { startService(RingService.command(this@RingActivity, RingService.ACTION_DISMISS)) },
                        shape = CircleShape,
                        border = BorderStroke(1.dp, NothingRed),
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                    ) {
                        Text(
                            stringResource(if (isAlarm) R.string.action_dismiss else R.string.action_stop).uppercase(),
                            color = NothingRed,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}
