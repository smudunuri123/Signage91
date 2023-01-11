package com.app.signage91.utils.extensions

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.PowerManager
import android.util.Log
import com.app.signage91.receivers.ScreenOffAdminReceiver

fun Context.turnOffScreen() {
    val policyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminReceiver = ComponentName(
        this,
        ScreenOffAdminReceiver::class.java
    )
    val admin = policyManager.isAdminActive(adminReceiver)
    if (admin) {
        policyManager.lockNow()
    } else {
        Log.i("TAG", "Not an admin")
    }
}

fun Context.turnOnScreen() {
    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
    val wl = pm.newWakeLock(
        PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
        "DEBUG: My Tag"
    )
    wl.acquire(1)
    if (wl.isHeld) wl.release()
}