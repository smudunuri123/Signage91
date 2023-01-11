package com.app.signage91.receivers

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.signage91.activities.SplashActivity2
import kotlinx.coroutines.delay

class WorkerClass (context : Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    val context : Context = context

    @SuppressLint("NewApi")
    override suspend fun doWork(): Result {
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        val taskList = activityManager!!.getRunningTasks(10)
        if (!taskList.isEmpty()){
            val runningTaskInfo = taskList[0]
            if (runningTaskInfo.topActivity != null &&
                    !runningTaskInfo.topActivity!!.className.contains(
                        "com.app.signage91.MainActivity"
                    )){
                val intent = Intent(context, SplashActivity2::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                delay(20000)
                applicationContext.startActivity(intent)
            }
        }
        return Result.success()
    }
}