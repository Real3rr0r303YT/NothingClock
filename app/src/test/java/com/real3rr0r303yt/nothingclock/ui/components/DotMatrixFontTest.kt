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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DotMatrixFontTest {

    @Test
    fun `all glyphs are 7 rows and rectangular`() {
        assertTrue(DotMatrixFont.isWellFormed())
    }

    @Test
    fun `covers every dot-matrix label of both languages`() {
        val labels = listOf(
            "CLOCK", "UHR", "ALARM", "WECKER", "TIMER", "STOPWATCH", "STOPPUHR", "SETTINGS", "EINSTELLUNGEN",
            "NOTHINGCLOCK", "07:05", "1:02:03.45", "00:00:00", "· · ·",
        )
        labels.flatMap { DotMatrixFont.normalize(it).toList() }.forEach { c ->
            assertTrue("missing glyph '$c'", DotMatrixFont.supports(c))
        }
    }

    @Test
    fun `monospaced digits keep running times from jittering`() {
        assertEquals(DotMatrixFont.measure("00:00", monoDigits = true), DotMatrixFont.measure("11:11", monoDigits = true))
        assertTrue(DotMatrixFont.measure("11:11") < DotMatrixFont.measure("00:00"))
    }
}
