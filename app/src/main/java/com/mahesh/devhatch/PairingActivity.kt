package com.mahesh.devhatch

import android.app.Activity
import android.os.Bundle

/**
 * Invisible trampoline for the widget's "Pair" tap. Starting a Settings screen
 * from a real Activity sidesteps the background-activity-launch limits that
 * would otherwise block a widget → BroadcastReceiver → startActivity path on
 * Android 12+. Does its work in onCreate and finishes immediately, so nothing
 * is drawn.
 */
class PairingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PairingLauncher.open(this)
        finish()
    }
}
