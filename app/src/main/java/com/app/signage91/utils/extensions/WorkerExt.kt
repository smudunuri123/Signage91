package com.app.signage91.utils.extensions

import android.content.Context
import androidx.work.*
import com.app.signage91.utils.Constants
import com.app.signage91.worker.SleepWakeUpWorker
import java.util.concurrent.TimeUnit


fun Context.startSleepWakeUpWorker() {
    val mWorkManager = WorkManager.getInstance(this)

    val myConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .build()
    /*val periodicSyncDataWork =
        PeriodicWorkRequest.Builder(SleepWakeUpWorker::class.java, 3, TimeUnit.HOURS)
            .setConstraints(myConstraints)
            .build()
    mWorkManager.enqueueUniquePeriodicWork(
        Constants.WORKER_TAG.SLEEP_WAKE_WORKER_TAG,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicSyncDataWork
    )*/
    val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SleepWakeUpWorker::class.java)
        .setConstraints(myConstraints)
        .setInitialDelay(20, TimeUnit.SECONDS)
        //.setInitialDelay(1, TimeUnit.MINUTES)
        .build()
    mWorkManager.enqueue(oneTimeWorkRequest)

    //mWorkManager.getWorkInfoById(oneTimeWorkRequest.id)
}