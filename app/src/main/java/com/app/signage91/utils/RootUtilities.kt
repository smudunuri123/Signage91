@file:Suppress("unused")

package com.app.signage91.utils

import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Locale


object RootUtilities {

    private fun isRooted(): Boolean {
        return findBinary()
    }


    private fun findBinary(): Boolean {
        var found = false
        if (!found) {
            val places = arrayOf("/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/")
            for (where in places) {
                if (File(where + "su").exists()) {
                    found = true
                    break
                }
            }
        }
        return found
    }


    fun findBinaryLocationPath(): String {
        var path = Constants.SYMBOL_HYPHEN
        var found = false
        if (!found) {
            val places = arrayOf("/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/")
            for (where in places) {
                if (File(where + "su").exists()) {
                    path = where + "su"
                    found = true
                    break
                }
            }
        }
        return path
    }


    fun isRootAvailableOnDevice(): Boolean {
        for (pathDir in System.getenv("PATH")?.split(":".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()!!) {
            if (File(pathDir, "su").exists()) {
                return true
            }
        }
        return false
    }


    fun isBusyBoxInstalled(): Boolean {
        return checkBinaryBusyBoxInstalled("busybox")
    }

    fun isTestKeysAvailable(): Boolean{
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        return false
    }

    fun isSuperUserAvailable():Boolean{
        try {
            val file = File("/system/app/Superuser.apk")
            if (file.exists()) {
                return true
            }
        } catch (e1: Exception) {
            // ignore
            return false
        }
        return false
    }

    fun canExecuteCommands() : Boolean{
        return canExecuteCommand("/system/xbin/which su")
                || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
    }

    // executes a command on the system
    private fun canExecuteCommand(command: String): Boolean {
        val executedSuccesfully: Boolean
        executedSuccesfully = try {
            Runtime.getRuntime().exec(command)
            true
        } catch (e: Exception) {
            false
        }
        return executedSuccesfully
    }
    private fun checkBinaryBusyBoxInstalled(filename: String): Boolean {
        val pathsArray = arrayOf("/data/local/", "/data/local/bin/", "/data/local/xbin/", "/sbin/", "/su/bin/", "/system/bin/", "/system/bin/.ext/", "/system/bin/failsafe/", "/system/sd/xbin/", "/system/usr/we-need-root/", "/system/xbin/")
        var result = false
        for (path in pathsArray) {
            val completePath = path + filename
            val f = File(completePath)
            val fileExists = f.exists()
            if (fileExists) {
                result = true
            }
        }
        return result
    }


    fun isRootGivenForDevice(): Boolean {
        if (isRootAvailableOnDevice()) {
            var process: Process? = null
            try {
                process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
                val input = BufferedReader(InputStreamReader(process!!.inputStream))
                val output = input.readLine()
                if (output != null && output.lowercase(Locale.getDefault()).contains("uid=0"))
                    return true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                process?.destroy()
            }
        }
        return false
    }


    val isOSSandAbove: Boolean
        get() {
            val sdkInt = Build.VERSION.SDK_INT
            var releaseInt = 0
            try {
                val releaseStr = Build.VERSION.RELEASE
                releaseInt = releaseStr.toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return sdkInt >= 31
        }


}