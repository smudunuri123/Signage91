package com.app.signage91.utils.extensions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import com.app.signage91.models.UserInfo
import com.app.signage91.receivers.ConnectivityReceiver


fun Context.addAppRestartAlarm() {
    val flags = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else -> PendingIntent.FLAG_UPDATE_CURRENT
    }
    val intent = Intent(applicationContext, ConnectivityReceiver::class.java)
    intent.action = "alarm.running"
    val pendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        0,
        intent,
        flags
    )
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    //alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 10 * 1000, pendingIntent);
    /*alarmManager.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 60 * 1000,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 60 * 1000,
                pendingIntent
            )
        }
    }*/
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        System.currentTimeMillis(),
        60 * 1000,
        pendingIntent
    )
}

fun Context.stopRestartAppAlarm(){
    val flags = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else -> PendingIntent.FLAG_UPDATE_CURRENT
    }
    val intent = Intent(this, ConnectivityReceiver::class.java)
    intent.action = "alarm.running"
    val pendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        0,
        intent,
        flags
    )
    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager?
    alarmManager?.cancel(pendingIntent)
}