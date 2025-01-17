package com.bnyro.clock.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.ui.dialog.AlarmReceiverDialog
import com.bnyro.clock.ui.dialog.TimerReceiverDialog
import com.bnyro.clock.ui.model.SettingsModel
import com.bnyro.clock.ui.nav.NavContainer
import com.bnyro.clock.ui.nav.NavRoutes
import com.bnyro.clock.ui.nav.bottomNavItems
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.ThemeUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsModel: SettingsModel = viewModel()

            val initialTab = when (intent?.action) {
                AlarmClock.ACTION_SET_ALARM, AlarmClock.ACTION_SHOW_ALARMS -> NavRoutes.Alarm
                AlarmClock.ACTION_SET_TIMER, AlarmClock.ACTION_SHOW_TIMERS -> NavRoutes.Timer
                else -> bottomNavItems.first {
                    Preferences.instance.getString(Preferences.startTabKey, NavRoutes.Alarm.route) == it.route
                }
            }

            val darkTheme = when (settingsModel.themeMode) {
                SettingsModel.Theme.SYSTEM -> isSystemInDarkTheme()
                SettingsModel.Theme.DARK, SettingsModel.Theme.AMOLED -> true
                else -> false
            }
            ClockYouTheme(
                darkTheme = darkTheme,
                customColorScheme = ThemeUtil.getSchemeFromSeed(
                    settingsModel.customColor,
                    darkTheme
                ),
                dynamicColor = settingsModel.colorTheme == SettingsModel.ColorTheme.SYSTEM,
                amoledDark = settingsModel.themeMode == SettingsModel.Theme.AMOLED
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    getInitialAlarm()?.let {
                        AlarmReceiverDialog(it)
                    }
                    getInitialTimer()?.let {
                        TimerReceiverDialog(it)
                    }
                    NavContainer(settingsModel, initialTab)
                }
            }

            LaunchedEffect(Unit) {
                requestNotificationPermissions()
            }
        }
    }

    private fun getInitialAlarm(): Alarm? {
        if (intent?.action != AlarmClock.ACTION_SET_ALARM) return null

        val hours = intent.getIntExtra(AlarmClock.EXTRA_HOUR, 0)
        val minutes = intent.getIntExtra(AlarmClock.EXTRA_MINUTES, 0)
        val days = intent.getIntArrayExtra(AlarmClock.EXTRA_DAYS)?.map { it - 1 }
        val ringingTone = intent.getStringExtra(AlarmClock.EXTRA_RINGTONE)
            .takeIf { it != AlarmClock.VALUE_RINGTONE_SILENT }

        return Alarm(
            time = ((hours * 60 + minutes) * 60000).toLong(),
            label = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE),
            enabled = false,
            days = days ?: listOf(0, 1, 2, 3, 4, 5, 6),
            repeat = days != null,
            soundUri = ringingTone,
            vibrate = intent.getBooleanExtra(AlarmClock.EXTRA_VIBRATE, false),
            soundEnabled = ringingTone != null
        )
    }

    private fun getInitialTimer(): Int? {
        if (intent?.action != AlarmClock.ACTION_SET_TIMER) return null

        return intent.getIntExtra(AlarmClock.EXTRA_LENGTH, 0).takeIf { it > 0 }
    }

    private fun requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }
}
