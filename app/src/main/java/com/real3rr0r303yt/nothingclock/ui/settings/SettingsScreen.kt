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

package com.real3rr0r303yt.nothingclock.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.real3rr0r303yt.nothingclock.BuildConfig
import com.real3rr0r303yt.nothingclock.R
import com.real3rr0r303yt.nothingclock.data.ClockSettings
import com.real3rr0r303yt.nothingclock.ui.components.CardContainer
import com.real3rr0r303yt.nothingclock.ui.components.ConfirmDialog
import com.real3rr0r303yt.nothingclock.ui.components.DotMatrixText
import com.real3rr0r303yt.nothingclock.ui.components.RedDot
import com.real3rr0r303yt.nothingclock.ui.components.SectionLabel
import com.real3rr0r303yt.nothingclock.ui.components.nothingCardStroke
import com.real3rr0r303yt.nothingclock.ui.theme.NothingBlack
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGrey
import com.real3rr0r303yt.nothingclock.ui.theme.NothingGreyDim
import com.real3rr0r303yt.nothingclock.ui.theme.NothingOutline
import com.real3rr0r303yt.nothingclock.ui.theme.NothingRed
import com.real3rr0r303yt.nothingclock.ui.theme.NothingWhite

const val DONATION_URL = "https://buymeacoffee.com/3rr0r303YT"

private enum class AppLanguage(val tag: String, val label: Int) {
    SYSTEM("", R.string.settings_language_system),
    GERMAN("de", R.string.settings_language_de),
    ENGLISH("en", R.string.settings_language_en),
}

@Composable
fun SettingsScreen(settings: ClockSettings, onChange: ((ClockSettings) -> ClockSettings) -> Unit) {
    val context = LocalContext.current
    var confirmDonate by remember { mutableStateOf(false) }
    val language = remember {
        val tag = AppCompatDelegate.getApplicationLocales()[0]?.language
        AppLanguage.entries.firstOrNull { it.tag.isNotEmpty() && it.tag == tag } ?: AppLanguage.SYSTEM
    }
    val noBrowser = stringResource(R.string.donate_no_browser)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 32.dp),
    ) {
        SectionLabel(stringResource(R.string.settings_section_language))
        SettingsCard(Modifier.selectableGroup()) {
            AppLanguage.entries.forEachIndexed { i, option ->
                if (i > 0) HorizontalDivider(color = NothingOutline)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(selected = option == language, role = Role.RadioButton) {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(option.tag))
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(option.label), Modifier.weight(1f), color = NothingWhite)
                    RadioButton(
                        selected = option == language,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = NothingRed, unselectedColor = NothingGreyDim),
                    )
                }
            }
        }

        SectionLabel(stringResource(R.string.settings_section_defaults))
        SettingsCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.settings_snooze), color = NothingWhite)
                Choices(listOf(5, 10, 15, 20), settings.snoozeMinutes, { stringResource(R.string.alarm_minutes, it) }) { v ->
                    onChange { it.copy(snoozeMinutes = v) }
                }
                Text(stringResource(R.string.settings_fade), color = NothingWhite, modifier = Modifier.padding(top = 8.dp))
                Choices(listOf(0, 15, 30, 60), settings.fadeInSeconds, {
                    if (it == 0) stringResource(R.string.alarm_fade_off) else stringResource(R.string.alarm_fade_seconds, it)
                }) { v -> onChange { it.copy(fadeInSeconds = v) } }
            }
            HorizontalDivider(color = NothingOutline)
            SwitchRow(stringResource(R.string.settings_vibrate), settings.vibrate) { v -> onChange { it.copy(vibrate = v) } }
        }

        SectionLabel(stringResource(R.string.settings_section_timer))
        SettingsCard {
            SwitchRow(stringResource(R.string.settings_timer_vibrate), settings.timerVibrate) { v -> onChange { it.copy(timerVibrate = v) } }
        }

        SectionLabel(stringResource(R.string.settings_section_privacy))
        SettingsCard(highlight = true) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.WifiOff, null, tint = NothingRed)
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.settings_offline_title), color = NothingWhite, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.settings_offline_text), color = NothingGrey, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        SectionLabel(stringResource(R.string.settings_section_support))
        SettingsCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.settings_donate_text), style = MaterialTheme.typography.bodySmall, color = NothingGrey)
                OutlinedButton(
                    onClick = { confirmDonate = true },
                    border = BorderStroke(1.dp, NothingOutline),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Icon(Icons.Outlined.Coffee, null, tint = NothingWhite, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_donate_title), color = NothingWhite)
                }
            }
        }

        SectionLabel(stringResource(R.string.settings_section_about))
        Column(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DotMatrixText("NOTHINGCLOCK", dotSize = 2.4.dp, gap = 1.dp)
                Spacer(Modifier.width(5.dp))
                RedDot(size = 4.dp)
            }
            Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.labelMedium, color = NothingGrey)
            Text(stringResource(R.string.settings_license), style = MaterialTheme.typography.labelSmall, color = NothingGreyDim)
            Text(stringResource(R.string.settings_copyright), style = MaterialTheme.typography.labelSmall, color = NothingGreyDim)
        }
    }

    if (confirmDonate) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_donate_title),
            text = stringResource(R.string.dialog_donate_text),
            confirmLabel = stringResource(R.string.action_open),
            destructive = false,
            onConfirm = {
                confirmDonate = false
                // The browser does the networking – this app has no INTERNET permission.
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DONATION_URL)).addCategory(Intent.CATEGORY_BROWSABLE)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, noBrowser, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { confirmDonate = false },
        )
    }
}

@Composable
private fun SettingsCard(modifier: Modifier = Modifier, highlight: Boolean = false, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = nothingCardStroke(highlight),
        colors = CardDefaults.cardColors(containerColor = CardContainer),
    ) { content() }
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = NothingWhite, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null,
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
}

@Composable
private fun Choices(options: List<Int>, selected: Int, label: @Composable (Int) -> String, onSelect: (Int) -> Unit) {
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
