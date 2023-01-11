package com.app.signage91.worker

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.app.signage91.utils.extensions.turnOffScreen
import java.util.concurrent.TimeUnit


class SleepWakeUpWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.i("tracer:", "Worker executed")

        applicationContext.turnOffScreen()
        return Result.success()
    }
}