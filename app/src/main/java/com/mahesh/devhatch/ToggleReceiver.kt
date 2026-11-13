package com.mahesh.devhatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Receives the widget buttons' [android.app.PendingIntent]s, performs the flag
 * write, then asks the widget to redraw so it shows live state.
 *
 * Handles only the flag-writing actions. The "pair" tap opens a Settings
 * screen and so goes through [PairingActivity] instead, to avoid
 * background-activity-launch limits on a receiver's `startActivity`.
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

    companion object {
        const val ACTION_TOGGLE_DEVOPTS = "com.mahesh.devhatch.TOGGLE_DEVOPTS"
        const val ACTION_TOGGLE_ADBWIFI = "com.mahesh.devhatch.TOGGLE_ADBWIFI"
        const val ACTION_BANKING_OFF = "com.mahesh.devhatch.BANKING_OFF"
    }
}
