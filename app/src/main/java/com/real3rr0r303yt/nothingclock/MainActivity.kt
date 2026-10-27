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

package com.real3rr0r303yt.nothingclock

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.AlarmClock
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableIntStateOf
import com.real3rr0r303yt.nothingclock.ui.ClockApp
import com.real3rr0r303yt.nothingclock.ui.theme.NothingClockTheme

/**
 * AppCompatActivity (instead of ComponentActivity) so that per-app language
 * switching via AppCompatDelegate also works on Android 8–12.
 */
class MainActivity : AppCompatActivity() {

    /** Tab requested via intent (e.g. tapping the system "next alarm" chip). */
    private val requestedTab = mutableIntStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK),
        )
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handle(intent)

        setContent {
            NothingClockTheme {
                ClockApp(requestedTab = requestedTab.intValue, onTabConsumed = { requestedTab.intValue = -1 })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    private fun handle(intent: Intent?) {
        requestedTab.intValue = when {
            intent?.action == AlarmClock.ACTION_SHOW_ALARMS -> TAB_ALARMS
            intent?.hasExtra(EXTRA_TAB) == true -> intent.getIntExtra(EXTRA_TAB, -1)
            else -> -1
        }
    }

    companion object {
        const val EXTRA_TAB = "tab"
        const val TAB_ALARMS = 1
    }
}
