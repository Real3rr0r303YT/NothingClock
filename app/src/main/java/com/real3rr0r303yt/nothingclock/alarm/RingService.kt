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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ServiceCompat
import com.real3rr0r303yt.nothingclock.clockApp
import com.real3rr0r303yt.nothingclock.core.Alarm
import com.real3rr0r303yt.nothingclock.core.CountdownTimer
import com.real3rr0r303yt.nothingclock.core.alarmVolume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground service that rings an alarm or finished timer: looping alarm tone with a
 * gentle fade-in, repeating vibration, full-screen notification and auto-silence.
 */
class RingService : Service() {

    enum class Kind { ALARM, TIMER }

    data class Ringing(val kind: Kind, val title: String, val alarm: Alarm?, val startedAt: Long)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var rampJob: Job? = null
    private var timeoutJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RING_ALARM -> {
                val alarm = alarmFrom(intent)
                start(Ringing(Kind.ALARM, alarm.label, alarm, SystemClock.elapsedRealtime()), alarm.vibrate, alarm.fadeInSeconds)
            }
            ACTION_RING_TIMER -> start(
                Ringing(Kind.TIMER, "", null, SystemClock.elapsedRealtime()),
                vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true),
                fadeInSeconds = 0,
            )
            ACTION_SNOOZE -> {
                _state.value?.alarm?.let { clockApp.scheduler.snooze(it, it.snoozeMinutes) }
                stopRinging()
            }
            ACTION_PLUS_MINUTE -> {
                val app = clockApp
                // App scope: this service stops right away and cancels its own scope.
                app.appScope.launch {
                    val timer = CountdownTimer().start(60_000, System.currentTimeMillis())
                    app.repository.setTimer(timer)
                    app.scheduler.scheduleTimer(timer.endsAt!!)
                }
                stopRinging()
            }
            ACTION_DISMISS -> stopRinging()
            else -> if (_state.value == null) stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun start(ring: Ringing, vibrate: Boolean, fadeInSeconds: Int) {
        releaseMedia() // a new alarm replaces whatever is ringing
        _state.value = ring
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
        } else {
            0
        }
        ServiceCompat.startForeground(this, Notifications.ID_RINGING, Notifications.ringing(this, ring), type)

        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NothingClock:ring")
            .apply { acquire(AUTO_SILENCE_MS + 5_000) }

        startSound(fadeInSeconds * 1000L)
        if (vibrate) startVibration()
        timeoutJob = scope.launch {
            delay(AUTO_SILENCE_MS)
            stopRinging()
        }
    }

    private fun startSound(fadeInMs: Long) {
        val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: return
        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(this@RingService, uri)
                isLooping = true
                val start = alarmVolume(0, fadeInMs)
                setVolume(start, start)
                prepare()
                start()
            }
        }.getOrNull()

        if (fadeInMs > 0) {
            rampJob = scope.launch {
                val began = SystemClock.elapsedRealtime()
                while (isActive) {
                    val v = alarmVolume(SystemClock.elapsedRealtime() - began, fadeInMs)
                    player?.setVolume(v, v)
                    if (v >= 1f) break
                    delay(200)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            getSystemService(Vibrator::class.java)
        }
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 600, 400, 600, 1200), 0)
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        vibrator?.vibrate(effect, attributes)
    }

    private fun stopRinging() {
        releaseMedia()
        _state.value = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releaseMedia() {
        rampJob?.cancel()
        timeoutJob?.cancel()
        player?.runCatching { stop(); release() }
        player = null
        vibrator?.cancel()
        vibrator = null
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    override fun onDestroy() {
        releaseMedia()
        _state.value = null
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_RING_ALARM = "com.real3rr0r303yt.nothingclock.ring.ALARM"
        const val ACTION_RING_TIMER = "com.real3rr0r303yt.nothingclock.ring.TIMER"
        const val ACTION_SNOOZE = "com.real3rr0r303yt.nothingclock.ring.SNOOZE"
        const val ACTION_DISMISS = "com.real3rr0r303yt.nothingclock.ring.DISMISS"
        const val ACTION_PLUS_MINUTE = "com.real3rr0r303yt.nothingclock.ring.PLUS_MINUTE"
        private const val EXTRA_VIBRATE = "vibrate"
        private const val AUTO_SILENCE_MS = 10 * 60_000L

        private val _state = MutableStateFlow<Ringing?>(null)
        /** What is ringing right now; observed by [RingActivity]. */
        val state: StateFlow<Ringing?> = _state.asStateFlow()

        fun alarmIntent(context: Context, alarm: Alarm): Intent = Intent(context, RingService::class.java)
            .setAction(ACTION_RING_ALARM)
            .putExtra("id", alarm.id)
            .putExtra("hour", alarm.hour)
            .putExtra("minute", alarm.minute)
            .putExtra("days", alarm.days)
            .putExtra("label", alarm.label)
            .putExtra("vibrate", alarm.vibrate)
            .putExtra("fade", alarm.fadeInSeconds)
            .putExtra("snooze", alarm.snoozeMinutes)

        fun timerIntent(context: Context, vibrate: Boolean): Intent =
            Intent(context, RingService::class.java).setAction(ACTION_RING_TIMER).putExtra(EXTRA_VIBRATE, vibrate)

        fun command(context: Context, action: String): Intent = Intent(context, RingService::class.java).setAction(action)

        private fun alarmFrom(i: Intent) = Alarm(
            id = i.getIntExtra("id", 0),
            hour = i.getIntExtra("hour", 0),
            minute = i.getIntExtra("minute", 0),
            days = i.getIntExtra("days", 0),
            label = i.getStringExtra("label").orEmpty(),
            vibrate = i.getBooleanExtra("vibrate", true),
            fadeInSeconds = i.getIntExtra("fade", 30),
            snoozeMinutes = i.getIntExtra("snooze", 10),
        )
    }
}
