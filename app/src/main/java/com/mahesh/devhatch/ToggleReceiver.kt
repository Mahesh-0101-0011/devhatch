package com.mahesh.devhatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Receives the widget buttons' [android.app.PendingIntent]s, performs the flag
 * write (or deep link), then asks the widget to redraw so it shows live state.
 *
 * Not exported: it is reached only via explicit intents from this app.
 */
class ToggleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TOGGLE_DEVOPTS ->
                guarded(context) {
                    SettingsWriter.setDevOptions(context, !SettingsWriter.isDevOptionsEnabled(context))
                }

            ACTION_TOGGLE_ADBWIFI ->
                guarded(context) {
                    SettingsWriter.setAdbWifi(context, !SettingsWriter.isAdbWifiEnabled(context))
                }

            ACTION_BANKING_OFF ->
                guarded(context) { SettingsWriter.bankingSafeOff(context) }

            ACTION_OPEN_PAIRING -> openPairing(context)
        }
        DevHatchWidget.refresh(context)
    }

    /** Run a write and warn (once) if the grant is missing. */
    private inline fun guarded(context: Context, write: () -> Boolean) {
        if (!SettingsWriter.hasWritePermission(context)) {
            Toast.makeText(context, R.string.toast_no_permission, Toast.LENGTH_LONG).show()
            return
        }
        write()
    }

    /**
     * Flow 3: make sure wireless debugging is on, then jump straight to the
     * pairing screen. The exact component is skin-specific, so try the known
     * AOSP wireless-debugging activity first and fall back to the Developer
     * options screen (a reliable public action) if it is not exposed.
     */
    private fun openPairing(context: Context) {
        if (SettingsWriter.hasWritePermission(context)) {
            SettingsWriter.setAdbWifi(context, true)
        }
        val candidates = listOf(
            Intent().setClassName(
                "com.android.settings",
                "com.android.settings.Settings\$WirelessDebuggingActivity"
            ),
            Intent(Settings_ACTION_DEV_SETTINGS)
        )
        for (candidate in candidates) {
            candidate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(candidate)
                return
            } catch (_: Exception) {
                // Try the next candidate.
            }
        }
        Toast.makeText(context, R.string.toast_no_pairing_screen, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val ACTION_TOGGLE_DEVOPTS = "com.mahesh.devhatch.TOGGLE_DEVOPTS"
        const val ACTION_TOGGLE_ADBWIFI = "com.mahesh.devhatch.TOGGLE_ADBWIFI"
        const val ACTION_BANKING_OFF = "com.mahesh.devhatch.BANKING_OFF"
        const val ACTION_OPEN_PAIRING = "com.mahesh.devhatch.OPEN_PAIRING"

        // android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
        private const val Settings_ACTION_DEV_SETTINGS =
            "android.settings.APPLICATION_DEVELOPMENT_SETTINGS"
    }
}
