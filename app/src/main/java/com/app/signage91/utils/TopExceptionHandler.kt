package com.app.signage91.utils

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import com.app.signage91.activities.SplashActivity2
import com.app.signage91.helpers.addLog
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlin.system.exitProcess


class TopExceptionHandler(var context: Context?) : Thread.UncaughtExceptionHandler {
    private val defaultUEH: Thread.UncaughtExceptionHandler =
        Thread.getDefaultUncaughtExceptionHandler()!!

    override fun uncaughtException(t: Thread, e: Throwable) {
        var arr = e.stackTrace
        var report = """
            $e
            """.trimIndent()
        report += "--------- Stack trace ---------\n\n"
        for (i in arr.indices) {
            report += """    ${arr[i]}
            """
        }
        report += "-------------------------------\n\n"

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n\n"
        val cause = e.cause
        if (cause != null) {
            report += """
                $cause
                """.trimIndent()
            arr = cause.stackTrace
            for (i in arr.indices) {
                report += """    ${arr[i]}
                """
            }
        }
        report += "-------------------------------\n\n"

        context?.addLog(report)

        Log.i("___CRASH", "CRASH")
        /*val intent = Intent(context, SplashActivity2::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)*/

        /*ProcessPhoenix.triggerRebirth(context)
        Process.killProcess(Process.myPid())
        exitProcess(2)*/


        defaultUEH.uncaughtException(t, e)
    }
}