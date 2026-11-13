package com.mahesh.devhatch

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

/**
 * Status screen and manual fallback for the widget. Shows whether the grant is
 * in place and the live state of both flags, plus buttons that do the same
 * work as the widget. The one-time `adb pm grant` command lives in the README,
 * since it is run from a computer, not here.
 *
 * Plain Views on purpose: a widget-first utility has no need for Compose on a
 * budget device.
 */
class MainActivity : Activity() {

    private lateinit var permissionStatus: TextView
    private lateinit var devState: TextView
    private lateinit var adbState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionStatus = findViewById(R.id.permission_status)
        devState = findViewById(R.id.dev_state)
        adbState = findViewById(R.id.adb_state)

        findViewById<Button>(R.id.btn_toggle_dev).setOnClickListener {
            ifGranted { SettingsWriter.setDevOptions(this, !SettingsWriter.isDevOptionsEnabled(this)) }
        }
        findViewById<Button>(R.id.btn_toggle_adb).setOnClickListener {
            ifGranted { SettingsWriter.setAdbWifi(this, !SettingsWriter.isAdbWifiEnabled(this)) }
        }
        findViewById<Button>(R.id.btn_banking_off).setOnClickListener {
            ifGranted { SettingsWriter.bankingSafeOff(this) }
        }
        findViewById<Button>(R.id.btn_open_pairing).setOnClickListener {
            PairingLauncher.open(this)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshState()
    }

    private inline fun ifGranted(write: () -> Unit) {
        if (!SettingsWriter.hasWritePermission(this)) {
            Toast.makeText(this, R.string.toast_no_permission, Toast.LENGTH_LONG).show()
            return
        }
        write()
        refreshState()
        DevHatchWidget.refresh(this)
    }

    private fun refreshState() {
        val granted = SettingsWriter.hasWritePermission(this)
        permissionStatus.text = getString(
            if (granted) R.string.permission_granted else R.string.permission_missing
        )
        devState.text = getString(
            R.string.row_dev_options,
            label(SettingsWriter.isDevOptionsEnabled(this))
        )
        adbState.text = getString(
            R.string.row_adb_wifi,
            label(SettingsWriter.isAdbWifiEnabled(this))
        )
    }

    private fun label(on: Boolean): String =
        getString(if (on) R.string.state_on else R.string.state_off)
}
