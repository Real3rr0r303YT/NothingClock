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

package com.real3rr0r303yt.nothingclock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGreyDim
import com.real3rr0r303yt.nothingclock.ui.theme.NothingOutline
import com.real3rr0r303yt.nothingclock.ui.theme.NothingRed
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * A ring of [count] dots – the Nothing-style seconds indicator / progress ring.
 * Dots before [active] are lit, the dot at [active] is red, the rest stay dim.
 */
@Composable
fun DotRing(
    active: Int,
    modifier: Modifier = Modifier,
    count: Int = 60,
    dotRadius: Dp = 2.5.dp,
    lit: Color = NothingGreyDim,
    dim: Color = NothingOutline,
    accent: Color = NothingRed,
) {
    Canvas(modifier) {
        val r = dotRadius.toPx()
        val radius = min(size.width, size.height) / 2 - r * 2
        val center = Offset(size.width / 2, size.height / 2)
        for (i in 0 until count) {
            val angle = Math.toRadians(i * 360.0 / count - 90)
            val p = Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())
            val color = when {
                i == active -> accent
                i < active -> lit
                else -> dim
            }
            drawCircle(color, if (i == active) r * 1.6f else r, p)
        }
    }
}
