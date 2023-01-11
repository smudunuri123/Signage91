package com.app.signage91.utils.extensions

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.app.signage91.BuildConfig
import com.app.signage91.helpers.createImageFile
import com.app.signage91.utils.RootInstaller
import com.app.signage91.utils.silent_app_install.ApkInstallUtils
import com.app.signage91.utils.silent_app_install.ShellUtils
import java.io.*
import java.util.*

fun Context.installApk() {
    val apkName = "TEST_DEBUG_APP_V2.apk"
    val assetManager: AssetManager = assets

    var `in`: InputStream? = null
    var out: OutputStream? = null

    try {
        val file = createImageFile(
            "Apks/",
            apkName
        )
        if (file?.exists()!!)
            file.delete()
        file.createNewFile()

        `in` = assetManager.open(apkName)
        out = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
        `in`.close()
        `in` = null
        out.flush()
        out.close()
        out = null

        RootInstaller.install(file.path)

    } catch (e: Exception) {
        Log.i("__ERROR", e.message.toString())
    }
}


private fun Context.runAdbCommands(commandString: String) {
    var p: Process? = null
    try {
        p = Runtime.getRuntime().exec("su")
        val outs = DataOutputStream(p.outputStream)
        //val cmd = "pm install /mnt/sdcard/app.apk"
        outs.writeBytes(commandString)
    } catch (e: IOException) {
        Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}


fun Context.installSilent(filePath: String?): Int {
    if (filePath == null) {
        return 1
    }
    /*val file = File(filePath)
    if (filePath.length <= 0 || !file.exists() || !file.isFile) {
        return 1
    }*/
    val args: Array<String>
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        args = arrayOf("pm", "install", "-i", BuildConfig.APPLICATION_ID, "--user", "0", filePath)
    } else {
        args = arrayOf("pm", "install", "-r", "-t", filePath)
    }
    val processBuilder = ProcessBuilder(*args)
    Log.i("version", "system code = " + Build.VERSION.SDK_INT)
    var process: Process? = null
    var successResult: BufferedReader? = null
    var errorResult: BufferedReader? = null
    val successMsg = java.lang.StringBuilder()
    val errorMsg = java.lang.StringBuilder()
    val result: Int
    try {
        process = processBuilder.start()
        successResult = BufferedReader(InputStreamReader(process.inputStream))
        errorResult = BufferedReader(InputStreamReader(process.errorStream))
        var s: String?
        while (successResult.readLine().also { s = it } != null) {
            successMsg.append(s)
        }
        while (errorResult.readLine().also { s = it } != null) {
            errorMsg.append(s)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        Log.d("version", "exception=>" + e.message)
    } finally {
        try {
            successResult?.close()
            errorResult?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        process?.destroy()
    }
    result = if (successMsg.toString().lowercase(Locale.getDefault()).contains("success")) {
        0
    } else {
        2
    }
    Log.d("version", "silence install successMsg: $successMsg , ErrorMsg: $errorMsg")
    Toast.makeText(
        this,
        "silence install successMsg: $successMsg , ErrorMsg: $errorMsg",
        Toast.LENGTH_SHORT
    ).show()
    return result
}


fun Context.installAPK(filePath: String) {
    // assume there is a apk file in /sdcard/ path
    val args = arrayOf("pm", "install", "-r", filePath)
    val processBuilder = ProcessBuilder(*args)
    var process: Process? = null
    var successResult: BufferedReader? = null
    var errorResult: BufferedReader? = null
    val successMsg = StringBuilder()
    val errorMsg = StringBuilder()
    try {
        process = processBuilder.start()
        successResult = BufferedReader(InputStreamReader(process.inputStream))
        errorResult = BufferedReader(InputStreamReader(process.errorStream))
        var s: String?
        while (successResult.readLine().also { s = it } != null) {
            successMsg.append(s)
        }
        while (errorResult.readLine().also { s = it } != null) {
            errorMsg.append(s)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            successResult?.close()
            errorResult?.close()
            process?.destroy()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    if (successMsg.toString().contains("success") || successMsg.toString().contains("Success")) {
        Toast.makeText(
            this,
            "Install APK success:)",
            Toast.LENGTH_LONG
        ).show()
    }
    Log.d("SilentInstall", "success msg:$successMsg")
    Log.d("SilentInstall", "error msg:$errorMsg")
}