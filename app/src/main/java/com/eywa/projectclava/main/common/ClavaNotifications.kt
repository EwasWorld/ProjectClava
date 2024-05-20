package com.eywa.projectclava.main.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.eywa.projectclava.R
import com.eywa.projectclava.main.mainActivity.MainActivity

class ClavaNotifications(val context: Context) {

    /**
     * Recommended to call on app start, safe to repeatedly call
     * https://developer.android.com/develop/ui/views/notifications/build-notification#Priority
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Match completion"
            val descriptionText = "Notifies when a match is about to finish"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(MATCH_COMPLETE_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager: NotificationManager = context.getSystemService()!!
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    fun createNotification(matchId: Int = 0, courtName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val extraFlags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE
            else 0
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or extraFlags,
        )

        val builder = NotificationCompat.Builder(context, MATCH_COMPLETE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Match ended")
            .setContentText("The match on $courtName is about to end")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(matchId, builder.build())
        }
    }

    companion object {
        const val MATCH_COMPLETE_CHANNEL_ID = "match_complete"
    }
}
