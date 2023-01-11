package com.app.signage91.utils.extensions

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity


fun Context.isAppRunning(packageName: String): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val procInfos = activityManager.runningAppProcesses
    if (procInfos != null) {
        for (processInfo in procInfos) {
            if (processInfo.processName == packageName) {
                return true
            }
        }
    }
    return false
}


fun Context.isAppOnForeground(): Boolean {
    val activityManager = getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val procInfos = activityManager.runningAppProcesses
        if (procInfos != null) {
            for (processInfo in procInfos) {
                if (processInfo.processName == packageName) {
                    return true
                }
            }
        }
    }else{
        val runningProcesses =  activityManager.getRunningTasks(100)
        for (processInfo in runningProcesses) {
            val activeProcess = processInfo.topActivity?.packageName
            if (activeProcess == packageName) {
                //If your app is the process in foreground, then it's not in running in background
                return true
            }
        }
    }
    return false
}

fun Context.isAppOnForeground2(): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcesses = activityManager.runningAppProcesses ?: return false
    for (appProcess in appProcesses) {
        val visibility = (appProcess.importance == IMPORTANCE_FOREGROUND || appProcess.importance == IMPORTANCE_VISIBLE)
        if (visibility && appProcess.processName == packageName) {
            return true
        }
    }
    return false
}

fun isForeground() : Boolean{
    val appProcessInfo = ActivityManager.RunningAppProcessInfo()
    ActivityManager.getMyMemoryState(appProcessInfo);
    return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE)
}