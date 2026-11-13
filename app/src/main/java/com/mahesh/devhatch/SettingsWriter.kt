package com.mahesh.devhatch

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

/**
 * Thin wrapper over the two `Settings.Global` integer flags DevHatch flips.
 *
 * The OS settings store is the single source of truth; DevHatch keeps no
 * local copy. Writing a flag needs [Manifest.permission.WRITE_SECURE_SETTINGS],
 * a signature-level permission granted once over adb (see README). Until then,
 * reads work but writes throw [SecurityException], which the write helpers
 * swallow and report as `false`.
 */
object SettingsWriter {

    /** Developer options master switch. Setting it to 1 makes the menu reappear. */
    const val KEY_DEV_OPTIONS = "development_settings_enabled"

    /** Wireless debugging (ADB over Wi-Fi). */
    const val KEY_ADB_WIFI = "adb_wifi_enabled"

    fun isDevOptionsEnabled(context: Context): Boolean = readFlag(context, KEY_DEV_OPTIONS)

    fun isAdbWifiEnabled(context: Context): Boolean = readFlag(context, KEY_ADB_WIFI)

    fun setDevOptions(context: Context, enabled: Boolean): Boolean =
        writeFlag(context, KEY_DEV_OPTIONS, enabled)

    fun setAdbWifi(context: Context, enabled: Boolean): Boolean =
        writeFlag(context, KEY_ADB_WIFI, enabled)

    /**
     * Turn both flags off in one shot, leaving the device in the exact state a
     * manual "off" would — the banking-safe action. Returns true only if both
     * writes succeed.
     */
    fun bankingSafeOff(context: Context): Boolean {
        val adb = setAdbWifi(context, false)
        val dev = setDevOptions(context, false)
        return adb && dev
    }

    /** Whether the one-time WRITE_SECURE_SETTINGS grant is in place. */
    fun hasWritePermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) ==
            PackageManager.PERMISSION_GRANTED

    private fun readFlag(context: Context, key: String): Boolean =
        Settings.Global.getInt(context.contentResolver, key, 0) == 1

    private fun writeFlag(context: Context, key: String, enabled: Boolean): Boolean =
        try {
            Settings.Global.putInt(context.contentResolver, key, if (enabled) 1 else 0)
        } catch (_: SecurityException) {
            false
        }
}
