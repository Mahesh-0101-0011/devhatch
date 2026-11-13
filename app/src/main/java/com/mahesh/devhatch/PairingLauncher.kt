package com.mahesh.devhatch

import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Opens the closest thing to the wireless-debugging/pairing screen that a
 * third-party app can reach, after ensuring wireless debugging is on.
 *
 * The dedicated Wireless Debugging screen (`WirelessDebuggingFragment`) is
 * hosted only by `com.android.settings.SubSettings`, which is not exported
 * (verified on Moto G05 / My UX: even an adb-shell `am start` is denied, and
 * the exported `.Settings` activity ignores `:settings:show_fragment` from an
 * outside caller and lands on the Settings homepage). So no third-party app
 * can open that screen directly.
 *
 * Best reachable target: Developer options scrolled to and highlighting the
 * "Wireless debugging" row via the `:settings:fragment_args_key` highlight
 * extra (honored by the Settings app since Android 7) — one tap from the
 * screen itself.
 */
object PairingLauncher {

    // android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
    private const val ACTION_DEV_SETTINGS =
        "android.settings.APPLICATION_DEVELOPMENT_SETTINGS"

    // Settings.EXTRA_FRAGMENT_ARG_KEY (public only since API 33; use the
    // literal so it works on minSdk 26). Scrolls to / highlights the
    // preference with this key on the opened screen.
    private const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"

    // The "Wireless debugging" row key in res/xml/development_settings.xml.
    private const val KEY_WIRELESS_DEBUG_ROW = "toggle_adb_wireless"

    fun open(context: Context) {
        if (SettingsWriter.hasWritePermission(context)) {
            SettingsWriter.setAdbWifi(context, true)
        }
        val candidates = listOf(
            // Some AOSP builds expose a direct alias; absent on My UX, so this
            // attempt throws and we fall through to the highlighted screen.
            Intent().setClassName(
                "com.android.settings",
                "com.android.settings.Settings\$WirelessDebuggingActivity"
            ),
            Intent(ACTION_DEV_SETTINGS).putExtra(EXTRA_FRAGMENT_ARG_KEY, KEY_WIRELESS_DEBUG_ROW),
            Intent(ACTION_DEV_SETTINGS)
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
}
