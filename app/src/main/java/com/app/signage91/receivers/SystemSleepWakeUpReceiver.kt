package com.app.signage91.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.signage91.R
import com.app.signage91.activities.SplashActivity
import com.app.signage91.utils.Constants
import com.app.signage91.utils.extensions.turnOffScreen
import com.app.signage91.utils.extensions.turnOnScreen


class SystemSleepWakeUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val timeInMillis = intent.getLongExtra(Constants.ALARM_MANAGER.KEY_TIME_IN_MILLIS, 0L)
        if (intent.action == Constants.ALARM_MANAGER.ACTION_SLEEP) {
            context.turnOffScreen()
            sendNotification(context, "System Sleep", timeInMillis);
        } else {
            context.turnOnScreen()
            sendNotification(context, "System Wake Up", timeInMillis);
        }
    }

    private fun sendNotification(context: Context, title: String, timeInMillis: Long?) {
        /*val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            Constants.NOTIFICATION_CHANNEL.SYSTEM_WAKE_UP
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Message")
            .setAutoCancel(true)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)
        val resultIntent = Intent(context, SplashActivity::class.java)

        val flags = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else -> PendingIntent.FLAG_UPDATE_CURRENT
        }
        val resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, flags)
        mBuilder.setContentIntent(resultPendingIntent)
        notificationManager.notify(999, mBuilder.build())*/


        val mBuilder = NotificationCompat.Builder(context.applicationContext, "notify_001")
        val ii = Intent(context.applicationContext, SplashActivity::class.java)
        val flags = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else -> PendingIntent.FLAG_UPDATE_CURRENT
        }
        val resultPendingIntent = PendingIntent.getActivity(context, 0, ii, flags)
        mBuilder.setContentIntent(resultPendingIntent)

        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        mBuilder.setContentTitle(title)
        mBuilder.setContentText(title)
        mBuilder.priority = Notification.PRIORITY_MAX

        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = Constants.NOTIFICATION_CHANNEL.SYSTEM_WAKE_UP_CHANNEL_ID
            val channel = NotificationChannel(
                channelId,
                Constants.NOTIFICATION_CHANNEL.SYSTEM_WAKE_UP,
                NotificationManager.IMPORTANCE_HIGH
            )
            mNotificationManager.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        mNotificationManager.notify(999, mBuilder.build())
    }

}