package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_REMINDER = "payment_reminders"
    const val CHANNEL_SUCCESS  = "payment_success"


    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDER,
                "Payment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminders when rent is due" }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SUCCESS,
                "Payment Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Confirmations when a payment is recorded" }
        )
    }

    fun show(
        context: Context,
        id: Int,
        channel: String,
        title: String,
        body: String,
        bigText: String? = null
    ) {
        ensureChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText ?: body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(
                if (channel == CHANNEL_REMINDER) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )

        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (_: SecurityException) { }
    }
}