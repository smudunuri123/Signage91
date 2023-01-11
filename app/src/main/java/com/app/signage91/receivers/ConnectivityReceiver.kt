package com.app.signage91.receivers

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import com.app.signage91.activities.MainActivity
import com.app.signage91.activities.SplashActivity2
import com.app.signage91.app.MyApplication
import com.app.signage91.utils.extensions.isAppOnForeground
import com.app.signage91.utils.extensions.isAppOnForeground2
import com.app.signage91.utils.extensions.isAppRunning
import com.jakewharton.processphoenix.ProcessPhoenix
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.exitProcess


class ConnectivityReceiver : BroadcastReceiver {
    constructor() : super() {}
    constructor(listener: ConnectivityReceiverListener?) {
        connectivityReceiverListener = listener
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                val i = Intent(context, SplashActivity2::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.applicationContext.startActivity(i)
            } else if (intent.action.equals("alarm.running")) {
                Log.d("Connectivity Receiver ", "Alarm manager ${intent.action}")
                /*val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager?
                val taskList = activityManager!!.getRunningTasks(10)
                if (!taskList.isEmpty()) {
                    val runningTaskInfo = taskList[0]
                    if (runningTaskInfo.topActivity != null &&
                        !runningTaskInfo.topActivity!!.className.contains(
                            "com.app.signage91"
                        )
                    ) {
                        if (!MainActivity.isResume) {
                            ProcessPhoenix.triggerRebirth(context)
                            Process.killProcess(Process.myPid())
                            exitProcess(2)
                        }
                    }
                }*/

                if (!(context.applicationContext as MyApplication).isApplicationActive){
                    ProcessPhoenix.triggerRebirth(context)
                    Process.killProcess(Process.myPid())
                    exitProcess(2)
                }

                /*if (!ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)){
                    ProcessPhoenix.triggerRebirth(context)
                }*/

                /*if (!context.isAppOnForeground()){
                    ProcessPhoenix.triggerRebirth(context)
                    Process.killProcess(Process.myPid())
                    exitProcess(2)
                }*/


                /*if (context.isBackgroundRunning()){
                    ProcessPhoenix.triggerRebirth(context)
                    Process.killProcess(Process.myPid())
                    exitProcess(2)
                }*/

                /*if(!(context.applicationContext as MyApplication).applicationActive){
                    ProcessPhoenix.triggerRebirth(context)
                    Process.killProcess(Process.myPid())
                    exitProcess(2)
                }*/

                /*val executor: ExecutorService = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                executor.execute {
                    val isRunningOnForeground = context.isAppOnForeground()
                    handler.post {
                        if (!isRunningOnForeground) {
                            ProcessPhoenix.triggerRebirth(context)
                            Process.killProcess(Process.myPid())
                            exitProcess(2)
                        }
                    }
                }*/
            } else {
                connectivityReceiverListener!!.onNetworkConnectionChanged(isConnected(context))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        @JvmField
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
        fun isConnected(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            @SuppressLint("MissingPermission") val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }
    }
}