package gl.joeppli.zueri.notify

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import gl.joeppli.zueri.MainActivity
import gl.joeppli.zueri.R

/**
 * Posts robot status updates to the system tray so they stay visible when
 * the app is backgrounded. A single notification id is reused, so the tray
 * never shows more than one order-status entry.
 */
object OrderNotifications {
    private const val CHANNEL_ID = "order_status"
    private const val NOTIFICATION_ID = 1

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    fun notifyStatus(context: Context, lang: String, text: String, arrived: Boolean = false) {
        if (!hasPermission(context)) return
        ensureChannel(context, lang)

        val title = when {
            arrived && lang == "en" -> "Jöppli has arrived!"
            arrived -> "Jöppli isch da!"
            lang == "en" -> "Your Jöppli is on its way"
            else -> "Dis Jöppli isch unterwegs"
        }
        val tapIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_joeppli)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(!arrived) // quiet status ticks, audible arrival
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // permission revoked mid-flight: drop silently
        }
    }

    private fun ensureChannel(context: Context, lang: String) {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(if (lang == "en") "Pickup updates" else "Abholig-Updates")
            .setDescription(
                if (lang == "en") "Live status of your recycling robot"
                else "Live-Status vo dim Recycling-Roboter"
            )
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}
