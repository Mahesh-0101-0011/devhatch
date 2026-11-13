package com.mahesh.devhatch

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews

/**
 * Home-screen widget (the primary surface). Renders the live state of the two
 * flags and four tap targets wired to [ToggleReceiver] via broadcast
 * [PendingIntent]s. Rebuilt on every toggle so it always reflects current
 * values.
 */
class DevHatchWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) renderWidget(context, appWidgetManager, id)
    }

    companion object {
        private const val COLOR_ON = 0xFF2E7D32.toInt()   // green 800
        private const val COLOR_OFF = 0xFF9E9E9E.toInt()   // grey 500

        /** Redraw every placed instance. Called after any flag write. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, DevHatchWidget::class.java)
            )
            for (id in ids) renderWidget(context, manager, id)
        }

        private fun renderWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_devhatch)

            val devOn = SettingsWriter.isDevOptionsEnabled(context)
            val adbOn = SettingsWriter.isAdbWifiEnabled(context)

            views.setTextViewText(R.id.devopts_state, stateLabel(context, devOn))
            views.setTextColor(R.id.devopts_state, if (devOn) COLOR_ON else COLOR_OFF)
            views.setTextViewText(R.id.adbwifi_state, stateLabel(context, adbOn))
            views.setTextColor(R.id.adbwifi_state, if (adbOn) COLOR_ON else COLOR_OFF)

            views.setOnClickPendingIntent(
                R.id.cell_devopts, broadcast(context, ToggleReceiver.ACTION_TOGGLE_DEVOPTS, 1)
            )
            views.setOnClickPendingIntent(
                R.id.cell_adbwifi, broadcast(context, ToggleReceiver.ACTION_TOGGLE_ADBWIFI, 2)
            )
            views.setOnClickPendingIntent(
                R.id.cell_banking, broadcast(context, ToggleReceiver.ACTION_BANKING_OFF, 3)
            )
            // "Pair" opens a Settings screen, so it launches an Activity
            // directly (via the invisible PairingActivity trampoline) rather
            // than a broadcast, avoiding background-activity-launch limits.
            views.setOnClickPendingIntent(R.id.cell_pairing, pairingActivity(context))

            manager.updateAppWidget(widgetId, views)
        }

        private fun stateLabel(context: Context, on: Boolean): String =
            context.getString(if (on) R.string.state_on else R.string.state_off)

        private fun broadcast(context: Context, action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, ToggleReceiver::class.java).setAction(action)
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun pairingActivity(context: Context): PendingIntent {
            val intent = Intent(context, PairingActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return PendingIntent.getActivity(
                context,
                4,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
